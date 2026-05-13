package ie.tcd.smartlock.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

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
    // 从配置文件中注入 RSA 公钥，用于 JWT 验证
    private final RSAPublicKey publicKey;
    // 从配置文件中注入 RSA 私钥，用于 JWT 签名
    private final RSAPrivateKey privateKey;

    public SecurityConfig(SmartLockProperties properties) {
        this.publicKey = properties.jwt().publicKey();
        this.privateKey = properties.jwt().privateKey();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        // 配置 Spring Security 的过滤链
        // @formatter:off 关闭IDEA自动格式化
            // 配置不需要登录的白名单 URL
        http.authorizeHttpRequests(authorize -> authorize
                    .requestMatchers( "/api/user/register", "/api/user/reset",
                            "/api/user/login", "/swagger-ui/**", "/v3/api-docs*/**").permitAll()
                    .anyRequest().authenticated())  // 其他任何URL都需要登录
            // 关闭 CSRF 防护
            .csrf(CsrfConfigurer::disable)
            // 发现 Authorization: Bearer 时触发 JWT 的认证逻辑
            .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(Customizer.withDefaults()))    // 自动寻找 JwtDecoder、公钥文件
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

    // 配置 JWT 编码器，用私钥对 JWT 进行签名
    @Bean
    JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(this.publicKey) // 构建 RSA 密钥对，其中包含公钥
                .privateKey(this.privateKey) // 设置私钥，用于签名
                .build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk)); // 创建一个 JWK (JSON Web Key) 集合
        return new NimbusJwtEncoder(jwks); // 返回基于 Nimbus 的 JWT 编码器
    }
}
