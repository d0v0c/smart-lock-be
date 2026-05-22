package ie.tcd.smartlock.app.controller;

import ie.tcd.smartlock.app.entity.Device;
import ie.tcd.smartlock.app.service.DeviceService;
import ie.tcd.smartlock.app.service.UserDeviceMergeService;
import ie.tcd.smartlock.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "ESP32设备管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/device")
public class DeviceController {
    private final DeviceService deviceService;
    private final UserDeviceMergeService userDeviceMergeService;

    @Operation(summary = "获取设备信息", description = "返回ESP32设备列表")
    @GetMapping
    public Result<List<Device>> getDeviceStatus(Authentication authentication) {
        return Result.success(deviceService.getByUsername(authentication.getName()));
    }

    @Operation(summary = "关联设备与账户")
    @PostMapping
    public Result<Void> linkUserAndDevice(@RequestBody Long deviceId, Authentication authentication) {
        String username = authentication.getName();
        userDeviceMergeService.linkUserAndDevice(username, deviceId);
        return Result.success(null);
    }
}
