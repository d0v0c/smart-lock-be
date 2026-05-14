package ie.tcd.smartlock.security;

import ie.tcd.smartlock.config.SmartLockProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.web.SecurityFilterChain;

import java.security.interfaces.EdECPrivateKey;
import java.security.interfaces.EdECPublicKey;

/**
 * @author xylingying
 * @date 2025-03-05 21:29
 * @description: Spring Security 配置类，
 * SecurityFilterChain 配置了拦截除登录注册外的所有URL响应；
 * UserDetailsService 配置了加载用户信息的方式是从数据库加载；
 * JWT 的编码器与解码器 配置了密钥从文件中加载；
 * PasswordEncoder 配置加密算法。
 */
@Configuration
public class SecurityConfig {
    public static final String TYP_CLAIM = "typ";
    public static final String TYP_ACCESS = "access";
    public static final String TYP_REFRESH = "refresh";
    private final EdECPrivateKey privateKey;
    private final EdECPublicKey publicKey;

    public SecurityConfig(SmartLockProperties properties) {
        this.privateKey = properties.jwt().privateKey();
        this.publicKey = properties.jwt().publicKey();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        // 配置 Spring Security 的过滤链
        // @formatter:off 关闭IDEA自动格式化
            // 配置不需要登录的白名单 URL
        http.authorizeHttpRequests(authorize -> authorize
                    .requestMatchers( "/api/user/register", "/api/user/reset",
                            "/api/user/login", "/api/user/refresh",
                            "/swagger-ui/**", "/v3/api-docs*/**").permitAll()
                    .anyRequest().authenticated())  // 其他任何URL都需要登录
            // 关闭 CSRF 防护
            .csrf(CsrfConfigurer::disable)
            // 发现 Authorization: Bearer 时触发 JWT 的认证逻辑
            .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.decoder(accessJwtDecoder())))    // 自动寻找 JwtDecoder、公钥文件
            // 设置会话管理策略为无状态（服务器不存 Session ID）
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        // @formatter:on
        return http.build();
    }

    // 配置密码加密算法用 BCrypt 算法
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 自定义 JWT 签名器，直接调用 JDK 自带的 java.security.Signature("Ed25519")
     * <br>
     * 彻底跳过 Nimbus 对 Ed25519 的诈骗式支持（底层调 Google Tink 库，不支持 Java 接口，函数直接抛异常）
     * 比方说 OctetKeyPair.java 如下：
     * public PublicKey toPublicKey() throws JOSEException {
     * throw new JOSEException("Export to java.security.PublicKey not supported");
     */
    @Bean
    JwtEncoder jwtEncoder() {
        return new EdDsaJwtEncoder(privateKey);
    }

    /**
     * 专门用来解码 access token 的 decoder，给 Spring Security 过滤链用。
     * 通过 typ=access 的 claim 校验器，防止有人拿 refresh token 直接调业务接口。
     * 一旦显式声明这个 Bean，Spring Boot 不再自动按 public-key-location 注册默认 decoder。
     */
    @Bean
    JwtDecoder accessJwtDecoder() {
        EdDsaJwtDecoder decoder = new EdDsaJwtDecoder(publicKey);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),                                  // 默认校验
                new JwtClaimValidator<String>(TYP_CLAIM, TYP_ACCESS::equals)    // 强制 typ=access
        ));
        return decoder;
    }

    /**
     * 专门用来解码 refresh token 的 decoder，给 /refresh 接口使用。
     * 通过 typ=refresh 的 claim 校验器，防止有人拿 access token 来换新 token。
     */
    @Bean("refreshJwtDecoder")
    JwtDecoder refreshJwtDecoder() {
        EdDsaJwtDecoder decoder = new EdDsaJwtDecoder(publicKey);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),
                new JwtClaimValidator<String>(TYP_CLAIM, TYP_REFRESH::equals)
        ));
        return decoder;
    }
}
