package ie.tcd.scss.smartdoorlockbe.controller;

import ie.tcd.scss.smartdoorlockbe.domain.Device;
import ie.tcd.scss.smartdoorlockbe.service.DeviceService;
import ie.tcd.scss.smartdoorlockbe.service.MqttMessageSender;
import ie.tcd.scss.smartdoorlockbe.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/device")
@Tag(name = "ESP32设备管理")
public class DeviceController {
    @Autowired
    private MqttMessageSender mqttMessageSender;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private UserDetailsService userDetailsService;

    //    @GetMapping("/api/{deviceId}/{isLocked}")
//    public String setLockedStatus(
//            @PathVariable("deviceId") String deviceId,
//            @PathVariable("isLocked") Integer isLocked) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("deviceId", deviceId);
//        map.put("isLocked", isLocked);
//        String json = JSON.toJSONString(map);
//        mqttMessageSender.send("server/lock", json);
//        return "json message sent.";
//    }
    @Operation(summary = "获取设备信息", description = "返回ESP32设备列表")
    @GetMapping
    public Result<List<Device>> getDeviceStatus(Authentication authentication) {
        return Result.success(deviceService.getByUsername(authentication.getName()));
    }
}
