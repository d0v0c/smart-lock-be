package ie.tcd.smartlock.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ie.tcd.smartlock.mapper.DeviceMapper;
import ie.tcd.smartlock.mapper.UserDeviceMergeMapper;
import ie.tcd.smartlock.model.entity.Device;
import ie.tcd.smartlock.model.entity.UserDeviceMerge;
import ie.tcd.smartlock.utils.BusinessException;
import ie.tcd.smartlock.utils.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author xylingying
 * @description 针对表【user_device_merge(用户与门禁的多对多关系)】的数据库操作Service
 * @createDate 2025-03-06 17:58:06
 */
@Service
@RequiredArgsConstructor
public class UserDeviceMergeService extends ServiceImpl<UserDeviceMergeMapper, UserDeviceMerge> {
    private final UserDeviceMergeMapper userDeviceMapper;
    private final DeviceMapper deviceMapper;

    public void linkUserAndDevice(String username, Long deviceId) {
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BusinessException(StatusCode.VALIDATION_ERROR, "No device found");
        }
        UserDeviceMerge userDevice = new UserDeviceMerge(username, deviceId);
        int res = userDeviceMapper.insert(userDevice);
        if (res == 0) {
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "Failed to link user and device");
        }
    }
}
