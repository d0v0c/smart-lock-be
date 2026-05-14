package ie.tcd.smartlock.controller;

import ie.tcd.smartlock.annotation.Debounce;
import ie.tcd.smartlock.model.entity.AccessCode;
import ie.tcd.smartlock.model.vo.req.AccessCodeGenerateReqVO;
import ie.tcd.smartlock.service.AccessCodeService;
import ie.tcd.smartlock.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "门锁密码管理")
@RestController
@RequestMapping("/api/code")
public class AccessCodeController {
    @Autowired
    private AccessCodeService accessCodeService;

    @Operation(summary = "生成密码", description = "发送设备+时间信息，服务器返回生成的密码")
    @Debounce(timeout = 31_000, releaseOnSuccess = true, key = "#request.deviceId")
    @PostMapping
    public Result<String> generateCode(@RequestBody @Validated AccessCodeGenerateReqVO request, Authentication authentication) {
        Long deviceId = request.getDeviceId();
        LocalDateTime validFrom = request.getValidFrom();
        LocalDateTime validTo = request.getValidTo();
        String owner = authentication.getName();
        String code = accessCodeService.generateCode(deviceId, validFrom, validTo, owner);
        return Result.success(code);
    }

    @Operation(summary = "获取密码", description = "返回用户的临时+永久所有密码")
    @GetMapping
    public Result<List<AccessCode>> getAccessCode(Authentication authentication) {
        return Result.success(accessCodeService.getByUsername(authentication.getName()));
    }
}
