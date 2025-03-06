package ie.tcd.scss.smartdoorlockbe.controller;

import ie.tcd.scss.smartdoorlockbe.service.AccessCodeService;
import ie.tcd.scss.smartdoorlockbe.utils.Result;
import ie.tcd.scss.smartdoorlockbe.vo.req.AccessCodeGenerateReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/code")
@Tag(name = "门锁密码管理")
public class AccessCodeController {
    @Autowired
    private AccessCodeService accessCodeService;

//    @GetMapping("/api/code/{deviceId}/{from}/{to}")
//    public String generateCode(
//            @PathVariable("deviceId") Long deviceId,
//            @PathVariable("from") ZonedDateTime from,
//            @PathVariable("to") ZonedDateTime to) {
//

    /// /        String passcode = accessCodeService.generateCode(deviceId, from, to);
//        return accessCodeService.generateCode(deviceId, from, to);
//    }
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
}
