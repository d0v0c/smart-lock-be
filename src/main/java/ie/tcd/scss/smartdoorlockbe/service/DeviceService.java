package ie.tcd.scss.smartdoorlockbe.service;

import ie.tcd.scss.smartdoorlockbe.domain.Device;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author xylingying
* @description 针对表【device(ESP32设备表)】的数据库操作Service
* @createDate 2025-02-21 00:58:41
*/
public interface DeviceService extends IService<Device> {
    void updateDeviceStatus(String payload);
}
