package ie.tcd.smartlock.app.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ie.tcd.smartlock.app.controller.vo.req.AccessCodeReqMqtt;
import ie.tcd.smartlock.app.controller.vo.resp.AccessCodeRespMqtt;
import ie.tcd.smartlock.app.entity.AccessCode;
import ie.tcd.smartlock.app.entity.Log;
import ie.tcd.smartlock.app.mapper.AccessCodeMapper;
import ie.tcd.smartlock.mqtt.MqttGateway;
import ie.tcd.smartlock.utils.BusinessException;
import ie.tcd.smartlock.utils.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
@Service
@Slf4j  // Lombok 注解，用于 log.info(...)、log.error(...)
public class AccessCodeService extends ServiceImpl<AccessCodeMapper, AccessCode> {
    @Autowired  // 自注入，解决 Spring AOP 的“自调用问题”（直接调用的话 @Cacheable、@CacheEvict会失效）
    @Lazy       // 避免循环依赖问题，运行到真正调用它的那一刻再去拿真实的代理对象
    private AccessCodeService self;
    @Autowired
    private MqttGateway mqttGateway;
    @Autowired
    private LogService logService;


    // 用于保存等待回复的 CompletableFuture，key 为 deviceId
    private final ConcurrentHashMap<Long, CompletableFuture<String>> pendingAccessCodeMap = new ConcurrentHashMap<>();

    /**
     * 发送 MQTT 消息给 MCU，并等待其返回确认消息
     */
    public String generateCode(Long deviceId, LocalDateTime from, LocalDateTime to, String owner) {
        if (from == null) from = LocalDateTime.now(ZoneOffset.UTC);
        // 生成 6 位随机数字
        String passcode = String.valueOf(100000 + new SecureRandom().nextInt(900000));

        // 生成唯一ID
        Long snowflakeId = IdUtil.getSnowflakeNextId();

        // 1. 构造发送给 MCU 的 JSON 消息
        AccessCodeReqMqtt codeReqMqtt = AccessCodeReqMqtt.builder()
                .deviceId(deviceId)
                .codeId(snowflakeId)
                .code(passcode)
                .validFrom(from)
                .validTo(to)
                .build();

        // 2. 建立 CompletableFuture 对象，放入等待队列中
        CompletableFuture<String> future = new CompletableFuture<>();
        try {
            pendingAccessCodeMap.put(snowflakeId, future);

            // 3. 发送 MQTT 消息到主题 "server/lock/code"
            mqttGateway.sendRpc("server/" + deviceId + "/code",
                    "device/" + deviceId + "/code", // 多实例部署时可以加上 /{instanceId}
                    ByteBuffer.allocate(8).putLong(snowflakeId).array(),    // 8 字节存 long
                    codeReqMqtt);

            // 4. 阻塞等待 MCU 回复确认消息，设置超时时间（例如 30 秒）
            String result = future.get(30, TimeUnit.SECONDS);

            if (!result.equals(passcode)) {  // 也有可能不必校验
                throw new RuntimeException("ESP32返回的密码与生成密码不一致");
            }
            // 5. 保存到数据库
            AccessCode accessCode = AccessCode.builder()
                    .codeId(snowflakeId)
                    .code(passcode)
                    .deviceId(deviceId)
                    .owner(owner)
                    .validFrom(from)
                    .validTo(to)
                    .build();

            self.saveAndEvict(accessCode);
            return passcode;
        } catch (TimeoutException e) {
            future.cancel(true); // 超时时取消任务
            throw new BusinessException(StatusCode.VALIDATION_ERROR, "Timeout: Waiting for ESP32 confirmation exceeded 30 seconds");
        } catch (Exception e) {
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "Failed to generate passcode" + e.getMessage());
        } finally {
            pendingAccessCodeMap.remove(snowflakeId);
        }
    }

    /**
     * 当接收到 MQTT 返回消息时调用此方法，解析 JSON 并通知等待线程
     */
    public void confirmUpdate(AccessCodeRespMqtt response, Long deviceId) {
        // 如果确认消息包含 codeId，则视为 MCU 的确认回复
        Long codeId = response.getCodeId();
        if (codeId == null) {
            log.warn("收到无 codeId 的 MCU 消息: {}", response);
            return;
        }
        // 将 future complete
        CompletableFuture<String> future = pendingAccessCodeMap.get(codeId);
        if (future == null) {
            log.warn("找不到 pending future, codeId={}", codeId);
            return;
        }
        future.complete(response.getCode());
        logService.save(new Log(Log.ActionType.CODE_CONFIRMATION, response.toString()));
    }

    /**
     * 使用 Spring Cache 自动缓存查询结果，key 使用 deviceId
     */
    @Cacheable(value = "AccessCode", key = "#deviceId")
    public List<AccessCodeReqMqtt> getAccessCodesCached(Long deviceId) {
        LambdaQueryWrapper<AccessCode> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessCode::getDeviceId, deviceId);
        List<AccessCode> list = this.list(queryWrapper);

        // BeanUtils.copyProperties(accessCode, req);
        return list.stream().map(AccessCodeReqMqtt::of).collect(Collectors.toList());
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
    public void getAllAccessCode(AccessCodeRespMqtt deviceIdResp, Long deviceId) {
        deviceIdResp.setDeviceId(deviceId);

        List<AccessCodeReqMqtt> reqMqtts = self.getAccessCodesCached(deviceId);

        mqttGateway.send("server/" + deviceId + "/all-code", reqMqtts);

        logService.save(new Log(Log.ActionType.GET_CODE, deviceIdResp.toString()));
    }


    public List<AccessCode> getByUsername(String username) {
        LambdaQueryWrapper<AccessCode> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessCode::getOwner, username);
        return this.list(queryWrapper);
    }
}