package ie.tcd.scss.smartdoorlockbe.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ie.tcd.scss.smartdoorlockbe.domain.Device;
import ie.tcd.scss.smartdoorlockbe.domain.UserDeviceMerge;
import ie.tcd.scss.smartdoorlockbe.mapper.DeviceMapper;
import ie.tcd.scss.smartdoorlockbe.service.DeviceService;
import ie.tcd.scss.smartdoorlockbe.service.UserDeviceMergeService;
import ie.tcd.scss.smartdoorlockbe.utils.BusinessException;
import ie.tcd.scss.smartdoorlockbe.utils.Result;
import ie.tcd.scss.smartdoorlockbe.utils.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xylingying
 * @description 针对表【device(ESP32设备表)】的数据库操作Service实现
 * @createDate 2025-02-21 00:58:41
 */
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {
    @Autowired
    private UserDeviceMergeService userDeviceMergeService;
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    /**
     * 设备刚上线，向 device/lock发消息时，解析 deviceId，同时将 isConnected设置为 true：
     * 如果设备不存在，就新增记录 create
     * 如果设备已存在，就更新记录 update
     */
    @Override
    public void updateDeviceStatus(String payload) {
        try {
            // 解析消息获取设备id和上线状态
            Device device = JSON.parseObject(payload, Device.class);
            device.setIsConnected(true);
            if (device.getDeviceId() == null) {
                throw new BusinessException(StatusCode.VALIDATION_ERROR, "deviceId 不能为空");
            }
            // 根据设备的id查询设备数据
            LambdaQueryWrapper<Device> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(Device::getDeviceId, device.getDeviceId());
            Device queriedDevice = this.getOne(lambdaQueryWrapper);
            if (queriedDevice == null) {        // 设备不存在，新增设备
                if (device.getDeviceName() == null) {
                    device.setDeviceName("ESP32_" + device.getDeviceId());
                }
                this.save(device);
            } else {     // 设备已经存在，修改设备的状态
                this.updateById(device);
            }

            LambdaQueryWrapper<UserDeviceMerge> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(UserDeviceMerge::getUsername);
            wrapper.eq(UserDeviceMerge::getDeviceId, device.getDeviceId());
            List<UserDeviceMerge> list = userDeviceMergeService.list(wrapper);

            Device deviceNotify = this.getOne(lambdaQueryWrapper);

            for (UserDeviceMerge userDeviceMerge : list) {
                System.out.println("向用户" + userDeviceMerge.getUsername() + "通知设备状态");
                messagingTemplate.convertAndSendToUser(userDeviceMerge.getUsername(), "/queue/is-locked", Result.success(deviceNotify));
            }
        } catch (BusinessException e) {
            System.out.println(e.getMessage());
        }
    }
}




