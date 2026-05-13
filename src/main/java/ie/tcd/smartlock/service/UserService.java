package ie.tcd.smartlock.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ie.tcd.smartlock.mapper.UserMapper;
import ie.tcd.smartlock.model.entity.User;
import ie.tcd.smartlock.utils.BusinessException;
import ie.tcd.smartlock.utils.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * @author xylingying
 * @description 针对表【user(后台用户表)】的数据库操作Service实现
 * @createDate 2025-03-05 16:04:41
 */
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

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

    public String login(User userReq) {
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

        // 校验成功，签发 JWT
        Instant now = Instant.now();
        //{
        //  "iss": "self",
        //  "sub": "username",
        //  "iat": 1641000000,
        //  "exp": 1641036000,
        //}
        JwtClaimsSet claims = JwtClaimsSet.builder()// 构建令牌的内容
                .issuer("self")                     // 当前服务是 JWT 的签发方
                .subject(user.getUsername())        // 主题 Subject（用户的唯一标识）是用户名。
                .issuedAt(now)                      // 当前时间签发
                .expiresAt(now.plusSeconds(36000L)) // 令牌的有效时间为 36000秒 (10小时)
                .build();
        // 将构建好的 JwtClaimsSet 声明封装为 JwtEncoderParameters，
        // 供编码器 JwtEncoder 进行编码和签名，生成最终的 JWT 字符串
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
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




