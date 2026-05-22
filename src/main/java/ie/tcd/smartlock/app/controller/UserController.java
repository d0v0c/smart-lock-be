package ie.tcd.smartlock.app.controller;

import ie.tcd.smartlock.app.controller.vo.req.RefreshReqVO;
import ie.tcd.smartlock.app.controller.vo.resp.LoginRespVO;
import ie.tcd.smartlock.app.controller.vo.resp.UserInfoRespVO;
import ie.tcd.smartlock.app.entity.User;
import ie.tcd.smartlock.app.entity.validation.LoginGroup;
import ie.tcd.smartlock.app.entity.validation.RegisterGroup;
import ie.tcd.smartlock.app.service.UserService;
import ie.tcd.smartlock.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author xylingying
 * @date 2025-03-05 17:07
 * @description: 用户管理
 */
@Tag(name = "用户管理")
@RestController // @RestController = @Controller + @ResponseBody，返回 JSON 而不是 .html 网页
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Operation(summary = "注册")
    @SecurityRequirements({})   // Swagger 不需要"挂锁"
    @PostMapping("/register") // = @RequestMapping(value = "/register", method = RequestMethod.POST)
    // @RequestBody, 空, @RequestParam, @PathVariable, @RequestHeader
    public Result<Void> register(@RequestBody @Validated({RegisterGroup.class, Default.class}) User user) {
        userService.register(user);
        return Result.success(null);
    }

    @Operation(summary = "登录", description = "返回 access token + refresh token")
    @SecurityRequirements({})
    @PostMapping("/login")
    public Result<LoginRespVO> login(@RequestBody @Validated({LoginGroup.class, Default.class}) User user) {
        LoginRespVO tokens = userService.login(user);
        return Result.success(tokens);
    }

    @Operation(summary = "刷新 token", description = "用 refresh token 换一对新的长短 token；旧 refresh 立即作废 (rotation)")
    @SecurityRequirements({})
    @PostMapping("/refresh")
    public Result<LoginRespVO> refresh(@RequestBody @Validated RefreshReqVO req) {
        LoginRespVO tokens = userService.refresh(req.getRefreshToken());
        return Result.success(tokens);
    }

    @Operation(summary = "登出", description = "删除当前用户的 refresh token，access token 只能自然过期")
    @PostMapping("/logout")
    public Result<Void> logout(Authentication authentication) {
        userService.logout(authentication.getName());
        return Result.success(null);
    }

    @Operation(summary = "获取用户信息")
    @GetMapping()
    public Result<UserInfoRespVO> getUser(Authentication authentication) {
        User user = userService.getById(authentication.getName());
        UserInfoRespVO userInfoRespVO = new UserInfoRespVO();
        BeanUtils.copyProperties(user, userInfoRespVO);
        return Result.success(userInfoRespVO);
    }

    @Operation(summary = "更新信息")
    @PutMapping()
    public Result<Void> updateUser(@RequestBody @Validated User user, Authentication authentication) {
        user.setUsername(authentication.getName());
        userService.update(user);
        return Result.success(null);
    }

    @Operation(summary = "找回密码")
    @SecurityRequirements({})
    @PutMapping("/reset")
    public Result<Void> resetPassword(@RequestBody @Validated({RegisterGroup.class, Default.class}) User user) {
        userService.resetPassword(user);
        return Result.success(null);
    }
}
