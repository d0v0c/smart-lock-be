package ie.tcd.scss.smartdoorlockbe.controller;

import ie.tcd.scss.smartdoorlockbe.domain.User;
import ie.tcd.scss.smartdoorlockbe.service.UserService;
import ie.tcd.scss.smartdoorlockbe.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * @author xylingying
 * @date 2025-03-05 17:07
 * @description: 用户管理
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理")
public class UserController {
    @Autowired
    private UserService userService;

    @Operation(summary = "注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Validated User user) {
        userService.register(user);
        return Result.success(null);
    }

    @Autowired
    JwtEncoder encoder;

    @Operation(summary = "登录")
    @GetMapping("/login")
    public Result<String> login(Authentication authentication) {
//        userService.login(user);
        // 通过FilterChain后签发JWT
        Instant now = Instant.now();
        long expiry = 36000L;    // 令牌的有效时间为 36000秒 (10小时)
        // @formatter:off
        String scope = authentication.getAuthorities().stream()	//获取用户的所有权限
                .map(GrantedAuthority::getAuthority)	//提取每个权限的字符串名称，如："ROLE_USER", "ROLE_ADMIN"
                .collect(Collectors.joining(" "));		//用空格分割，最终得到 "ROLE_USER ROLE_ADMIN"。
        JwtClaimsSet claims = JwtClaimsSet.builder()	// 构建令牌的内容
                .issuer("self")		//当前服务是 JWT 的签发方
                .issuedAt(now)		//当前时间签发
                .expiresAt(now.plusSeconds(expiry))	//10小时后过期
                .subject(authentication.getName())	//主题Subject（用户的唯一标识）是用户名。
                .claim("scope", scope)	// 自定义声明，设置解析出的用户的权限scope
                .build();
        //将构建好的 JwtClaimsSet 声明封装为 JwtEncoderParameters，
        // 供编码器JwtEncoder进行编码和签名，生成最终的 JWT 字符串
        //{
        //  "iss": "self",
        //  "iat": 1641000000,
        //  "exp": 1641036000,
        //  "sub": "username",
        //  "scope": "ROLE_USER ROLE_ADMIN"
        //}
        // @formatter:on
        return Result.success(
                this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue()
        );
    }

//    @GetMapping("/")
//    public String hello(Authentication authentication) {
//        return "Hello, " + authentication.getName() + "!";
//    }
}
