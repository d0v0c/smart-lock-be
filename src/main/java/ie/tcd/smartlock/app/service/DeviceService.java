package ie.tcd.smartlock.app.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ie.tcd.smartlock.app.entity.Device;
import ie.tcd.smartlock.app.entity.Log;
import ie.tcd.smartlock.app.mapper.DeviceMapper;
import ie.tcd.smartlock.app.mapper.UserDeviceMergeMapper;
import ie.tcd.smartlock.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xylingying
 * @description 针对表【device(ESP32设备表)】的数据库操作Service实现
 * @createDate 2025-02-21 00:58:41
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceService extends ServiceImpl<DeviceMapper, Device> {
    private final DeviceMapper deviceMapper;
    private final UserDeviceMergeMapper userDeviceMapper;
    private final SimpMessageSendingOperations messagingTemplate;
    private final LogService logService;

    /**
     * 设备刚上线，向 device/lock发消息时，解析 deviceId，同时将 isConnected设置为 true：
     * 如果设备不存在，就新增记录 create
     * 如果设备已存在，就更新记录 update
     */
    public void updateDeviceStatus(Device device, Long deviceId) {
        // 解析消息获取设备id和上线状态
        device.setIsConnected(true);
//            device.setDeviceId(deviceId); 就等着抛异常

        // 根据设备的id查询设备数据
        Device queriedDevice = deviceMapper.selectById(deviceId);

        if (queriedDevice == null) {        // 设备不存在，新增设备
            if (device.getDeviceName() == null) {
                device.setDeviceName("ESP32_" + deviceId);
            }
            deviceMapper.insert(device);
            logService.save(new Log(Log.ActionType.NEW_DEVICE, device.toString()));
        } else {     // 设备已经存在，修改设备的状态
            deviceMapper.updateById(device);
            logService.save(new Log(Log.ActionType.UPDATE_DEVICE, device.toString()));
        }
        // 查询device对应的用户
        List<String> usernames = userDeviceMapper.selectUsernamesByDeviceId(deviceId);
        // 设备信息通知给用户
        for (String username : usernames) {
            log.info("向用户 {} 通知设备状态", username);
            // WebSocket
            messagingTemplate.convertAndSendToUser(username, "/queue/is-locked", Result.success(device));
        }
    }

    public List<Device> getByUsername(String username) {
        return deviceMapper.selectByUsername(username);
    }
}




