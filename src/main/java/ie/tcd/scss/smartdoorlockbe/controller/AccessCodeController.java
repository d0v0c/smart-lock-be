package ie.tcd.scss.smartdoorlockbe.controller;

import ie.tcd.scss.smartdoorlockbe.domain.AccessCode;
import ie.tcd.scss.smartdoorlockbe.service.AccessCodeService;
import ie.tcd.scss.smartdoorlockbe.utils.Result;
import ie.tcd.scss.smartdoorlockbe.vo.req.AccessCodeGenerateReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/code")
@Tag(name = "门锁密码管理")
public class AccessCodeController {
    @Autowired
    private AccessCodeService accessCodeService;

    @Operation(summary = "生成密码", description = "发送设备+时间信息，服务器返回生成的密码")
    @PostMapping
    public Result<String> generateCode(@RequestBody @Validated AccessCodeGenerateReqVO request, Authentication authentication) {
        Long deviceId = request.getDeviceId();
        ZonedDateTime validFrom = request.getValidFrom();
        ZonedDateTime validTo = request.getValidTo();
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
