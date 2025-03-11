package ie.tcd.scss.smartdoorlockbe.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ie.tcd.scss.smartdoorlockbe.domain.AccessCode;
import ie.tcd.scss.smartdoorlockbe.mapper.AccessCodeMapper;
import ie.tcd.scss.smartdoorlockbe.service.AccessCodeService;
import ie.tcd.scss.smartdoorlockbe.service.MqttMessageSender;
import ie.tcd.scss.smartdoorlockbe.utils.BusinessException;
import ie.tcd.scss.smartdoorlockbe.utils.StatusCode;
import ie.tcd.scss.smartdoorlockbe.vo.req.AccessCodeReqMqtt;
import ie.tcd.scss.smartdoorlockbe.vo.resp.AccessCodeRespMqtt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * @author xylingying
 * @description 针对表【access_code(门锁密码表)】的数据库操作Service实现
 * @createDate 2025-02-22 16:15:31
 */
@Slf4j
@Service
public class AccessCodeServiceImpl extends ServiceImpl<AccessCodeMapper, AccessCode> implements AccessCodeService {
    // 注入自身代理对象
    @Autowired
    @Lazy // 避免循环依赖问题，如果启动时报错，通常可加此注解解决
    private AccessCodeServiceImpl self;
    @Autowired
    private MqttMessageSender mqttMessageSender;
    // 用于保存等待回复的 CompletableFuture，key 为 deviceId
    private final ConcurrentHashMap<Long, CompletableFuture<String>> pendingAccessCodeMap = new ConcurrentHashMap<>();

    /**
     * 发送 MQTT 消息给 MCU，并等待其返回确认消息
     */
    @Override
    public String generateCode(Long deviceId, ZonedDateTime from, ZonedDateTime to, String owner) {
        // 生成 6 位随机数字
        SecureRandom secureRandom = new SecureRandom();
        int randomPassword = 100000 + secureRandom.nextInt(900000);
        String passcode = String.valueOf(randomPassword);
        System.out.println("随机密码是: " + passcode);

        // 生成唯一ID
        Long snowflakeId = IdUtil.getSnowflakeNextId();

        // 1. 构造发送给 MCU 的 JSON 消息
        AccessCodeReqMqtt request = new AccessCodeReqMqtt();
        request.setDeviceId(deviceId);
        request.setCodeId(snowflakeId);
        request.setCode(passcode);
        request.setValidFrom(from);
        request.setValidTo(to);
        String json = JSON.toJSONString(request);

        // 2. 建立 CompletableFuture 对象，放入等待队列中
        CompletableFuture<String> future = new CompletableFuture<>();
        try {
            pendingAccessCodeMap.put(snowflakeId, future);

            // 3. 发送 MQTT 消息到主题 "server/lock/code"
            mqttMessageSender.send("server/lock/code", json);

            // 4. 阻塞等待 MCU 回复确认消息，设置超时时间（例如 30 秒）
            String result = future.get(30, TimeUnit.SECONDS);

            if (!result.equals(passcode)) {  // 也有可能不必校验
                throw new RuntimeException("ESP32返回的密码与生成密码不一致");
            }
            // 5. 保存到数据库
            AccessCode accessCode = new AccessCode();
            accessCode.setCodeId(snowflakeId);
            accessCode.setCode(passcode);
            accessCode.setDeviceId(deviceId);
            accessCode.setOwner(owner);
            accessCode.setValidFrom(from);
            accessCode.setValidTo(to);
            self.saveAndEvict(accessCode);
            return passcode;
        } catch (TimeoutException e) {
            future.cancel(true); // 超时时取消任务
            throw new BusinessException(StatusCode.VALIDATION_ERROR, "超时：等待ESP32确认超过30秒");
        } catch (Exception e) {
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "生成门锁密码失败：" + e.getMessage());
        } finally {
            pendingAccessCodeMap.remove(snowflakeId);
        }
    }

    /**
     * 当接收到 MQTT 返回消息时调用此方法，解析 JSON 并通知等待线程
     */
    @Override
    public void confirmUpdate(String payload) {
        try {
            AccessCodeRespMqtt response = JSON.parseObject(payload, AccessCodeRespMqtt.class);
            // 如果确认消息包含 codeId，则视为 MCU 的确认回复
            if (response.getCodeId() != null) {
                // 将 future complete
                CompletableFuture<String> future = pendingAccessCodeMap.get(response.getCodeId());
                if (future != null) {
                    future.complete(response.getCode());
                } else {
                    System.out.println("收到MCU确认，但找不到对应的 pending future，codeId: " + response.getCodeId());
                }
            } else {
                System.out.println("无CodeId: " + payload);
            }
        } catch (Exception e) {
            System.out.println("解析MCU确认消息失败");
            log.error(e.getMessage());
        }
    }

    /**
     * 使用 Spring Cache 自动缓存查询结果，key 使用 deviceId
     */
    @Cacheable(value = "AccessCode", key = "#deviceId")
    public List<AccessCodeReqMqtt> getAccessCodesCached(Long deviceId) {
        LambdaQueryWrapper<AccessCode> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessCode::getDeviceId, deviceId);
        List<AccessCode> list = this.list(queryWrapper);

        return list.stream().map(accessCode -> {
            AccessCodeReqMqtt req = new AccessCodeReqMqtt();
            BeanUtils.copyProperties(accessCode, req);
            return req;
        }).collect(Collectors.toList());
    }

    /**
     * 使用 Spring Cache 自动更新数据库写入后 redis 缓存，key 使用 deviceId
     */
    @CacheEvict(value = "AccessCode", key = "#accessCode.deviceId")
    public boolean saveAndEvict(AccessCode accessCode) {
        return this.save(accessCode);
    }

    /**
     * 接收 deviceId
     * 从数据库查询code validFrom validTo -> 从redis查询
     * 包装成List<AccessCodeReqMqtt>返回给ESP32
     */
    public void getAllAccessCode(String payload) {
        AccessCodeRespMqtt deviceIdResp = JSON.parseObject(payload, AccessCodeRespMqtt.class);
        Long deviceId = deviceIdResp.getDeviceId();

        List<AccessCodeReqMqtt> reqMqtts = self.getAccessCodesCached(deviceId);

        mqttMessageSender.send("server/lock/all-code", JSON.toJSONString(reqMqtts));
    }
}