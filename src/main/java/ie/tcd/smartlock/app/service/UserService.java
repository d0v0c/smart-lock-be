package ie.tcd.smartlock.app.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ie.tcd.smartlock.app.controller.vo.resp.LoginRespVO;
import ie.tcd.smartlock.app.entity.User;
import ie.tcd.smartlock.app.mapper.UserMapper;
import ie.tcd.smartlock.config.SmartLockProperties;
import ie.tcd.smartlock.security.SecurityConfig;
import ie.tcd.smartlock.utils.BusinessException;
import ie.tcd.smartlock.utils.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * @author xylingying
 * @description 针对表【user(后台用户表)】的数据库操作Service实现
 * @createDate 2025-03-05 16:04:41
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {
    // refresh token 在 Redis 中的 Key 前缀
    private static final String REFRESH_KEY_PREFIX = "refresh:";
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder refreshJwtDecoder;
    private final StringRedisTemplate stringRedisTemplate;
    private final SmartLockProperties properties;

    public void register(User userReq) {
        // 查询用户名是否重复
        User user = userMapper.selectById(userReq.getUsername());
        if (user != null) {
            throw new BusinessException(StatusCode.VALIDATION_ERROR, "The account already exists");
        }
        // 加密登录密码
        userReq.setPassword(passwordEncoder.encode(userReq.getPassword()));
        int res = userMapper.insert(userReq);
        if (res == 0) {
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "Failed to insert data");
        }
    }

    public LoginRespVO login(User userReq) {
        // 查询用户名是否存在
        // @Select("SELECT username, password FROM user WHERE username = #{username}")
//        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//        lambdaQueryWrapper.select(User::getUsername, User::getPassword)
//                .eq(User::getUsername, request.getUsername());
//        User user = this.getOne(lambdaQueryWrapper);
        User user = userMapper.selectById(userReq.getUsername());
        if (user == null) {
            throw new BusinessException(StatusCode.VALIDATION_ERROR, "Invalid username or password");   // 用户名不存在
        }
        if (!passwordEncoder.matches(userReq.getPassword(), user.getPassword())) {
            throw new BusinessException(StatusCode.VALIDATION_ERROR, "Invalid username or password");   // 密码错误
        }

        // 校验成功，签发一对长短 JWT。
        // 单设备策略：写 Redis 时直接覆盖旧 jti，相当于把上一个设备的 refresh 作废。
        return issueTokenPair(user.getUsername());
    }

    /**
     * 用 refresh token 换一对新的长短 token，并轮换 (rotation) 掉旧的 refresh。
     * 流程：解码并校验签名/过期/typ → 比对 Redis 里登记的 jti → 重新签发并覆盖。
     */
    public LoginRespVO refresh(String refreshToken) {
        Jwt decoded;
        try {
            decoded = refreshJwtDecoder.decode(refreshToken);
        } catch (JwtException e) {
            throw new BusinessException(StatusCode.TOKEN_INVALID, "Invalid refresh token");
        }

        String username = decoded.getSubject();
        String jti = decoded.getId();
        if (username == null || jti == null) {
            throw new BusinessException(StatusCode.TOKEN_INVALID, "Invalid jti");
        }

        // 比对 Redis 里登记的 jti——一旦发生过 rotation 或 logout，旧 jti 就和 Redis 里对不上
        String storedJti = stringRedisTemplate.opsForValue().get(REFRESH_KEY_PREFIX + username);
        if (storedJti == null || !storedJti.equals(jti)) {
            throw new BusinessException(StatusCode.TOKEN_INVALID, "Refresh token has been revoked");
        }

        // rotation：签发新的一对 token，覆盖旧 jti
        return issueTokenPair(username);
    }

    /**
     * 删除当前用户在 Redis 中登记的 refresh jti，使长 token 立即失效。
     */
    public void logout(String username) {
        stringRedisTemplate.delete(REFRESH_KEY_PREFIX + username);
    }


    // 签发 access + refresh，并把新 refresh 的 jti 覆盖式写入 Redis。
    private LoginRespVO issueTokenPair(String username) {
        Instant now = Instant.now();
        Duration accessTtl = properties.jwt().accessTtl();
        Duration refreshTtl = properties.jwt().refreshTtl();

        // 生成 access token
        //{
        //  "iss": "self",
        //  "sub": "username",
        //  "typ": "access",
        //  "iat": 1641000000,
        //  "exp": 1641036000,
        //}
        JwtClaimsSet accessClaims = JwtClaimsSet.builder()  // 构建令牌
                .issuer("self")                             // JWT 的签发方是当前服务器
                .subject(username)                          // 主题（用户的唯一标识）是用户名
                .issuedAt(now)
                .expiresAt(now.plus(accessTtl))
                .claim(SecurityConfig.TYP_CLAIM, SecurityConfig.TYP_ACCESS)
                .build();
        // 自定义 EdDsaJwtEncoder 内部固定用 EdDSA 签名，调用方无需再传 JwsHeader
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(accessClaims)).getTokenValue();

        // 生成 refresh token
        String jti = String.valueOf(IdUtil.getSnowflakeNextId());
        JwtClaimsSet refreshClaims = JwtClaimsSet.builder()
                .issuer("self")
                .subject(username)
                .issuedAt(now)
                .expiresAt(now.plus(refreshTtl))
                .id(jti)
                .claim(SecurityConfig.TYP_CLAIM, SecurityConfig.TYP_REFRESH)
                .build();
        String refreshToken = jwtEncoder.encode(JwtEncoderParameters.from(refreshClaims)).getTokenValue();

        // Redis 覆盖旧 jti，旧 refresh 立刻作废
        stringRedisTemplate.opsForValue().set(
                REFRESH_KEY_PREFIX + username,
                jti,
                refreshTtl.toSeconds(),
                TimeUnit.SECONDS
        );

        return new LoginRespVO(accessToken, refreshToken);
    }

    public void update(User userReq) {
        if (userReq.getPassword() != null) {
            userReq.setPassword(passwordEncoder.encode(userReq.getPassword()));
        }
        int res = userMapper.updateById(userReq);
        if (res == 0) {
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "Failed to update data");
        }
    }

    public void resetPassword(User userReq) {
        User storedUser = userMapper.selectById(userReq.getUsername());
        if (storedUser == null) {
            throw new BusinessException(StatusCode.VALIDATION_ERROR, "The username does not exist");
        }
        if (!storedUser.getEmail().equals(userReq.getEmail())) {
            throw new BusinessException(StatusCode.VALIDATION_ERROR, "Email address does not match the username");
        }
        userReq.setPassword(passwordEncoder.encode(userReq.getPassword()));
        int res = userMapper.updateById(userReq);
        if (res == 0) {
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "Failed to reset password");
        }
    }
}
