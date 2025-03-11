package ie.tcd.scss.smartdoorlockbe.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import ie.tcd.scss.smartdoorlockbe.domain.User;
import ie.tcd.scss.smartdoorlockbe.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * @author xylingying
 * @date 2025-03-05 21:29
 * @description: Spring Security 配置类，
 * SecurityFilterChain 配置了拦截除登录注册外的所有URL响应
 * UserDetailsService 配置了加载用户信息的方式是从数据库加载
 * JWT 的编码器与解码器 配置了密钥从文件中加载
 * PasswordEncoder 配置
 */
@Configuration
public class SecurityConfig {

    @Value("${jwt.public.key}") // 从配置文件中注入 RSA 公钥，用于 JWT 验证
    RSAPublicKey key;

    @Value("${jwt.private.key}") // 从配置文件中注入 RSA 私钥，用于 JWT 签名
    RSAPrivateKey priv;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 配置 Spring Security 的过滤链
        // @formatter:off
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers( "/api/user/register", "/api/user/reset", "/swagger-ui/**", "/v3/api-docs*/**").permitAll()
                        .anyRequest().authenticated() // 任何请求都需要认证 authenticated()，除了注册
                )
                .csrf((csrf) -> csrf
                        .disable())
                        //.ignoringRequestMatchers("/api/user/login", "/api/user/register")) // 禁用注册登录端点上的 CSRF 防护
                // 发现 Basic 时触发账号密码认证逻辑，默认配置
                .httpBasic(Customizer.withDefaults())
                // 发现 Bearer 时触发 JWT 的认证逻辑
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder())))
                // 设置会话管理策略为无状态（REST API 推荐无状态，以支持水平扩展）
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()) // 设置认证入口点，处理未认证的请求
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler()) // 设置拒绝访问处理器，处理权限不足的请求
                );
        // @formatter:on
        return http.build(); // 构建并返回过滤链
    }

    @Autowired
    private UserMapper userMapper;

    // 配置密码加密算法用 BCrypt 算法
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        return username -> {
            // 从数据库加载用户信息
            User dbUser = userMapper.selectById(username);
            if (dbUser == null) {
                throw new UsernameNotFoundException("User '" + username + "' 找不到");
            }
            // 根据从数据库获取的用户数据构建 UserDetails 对象
            return org.springframework.security.core.userdetails.User.withUsername(dbUser.getUsername())
                    .password(dbUser.getPassword())
                    .authorities("app") // 不考虑权限
                    .build();
        };
    }

    @Bean
    JwtDecoder jwtDecoder() {
        // 创建 JWT 解码器，用公钥验证 JWT 的签名
        return NimbusJwtDecoder.withPublicKey(this.key).build();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        // 配置 JWT 编码器，用私钥对 JWT 进行签名
        JWK jwk = new RSAKey.Builder(this.key) // 构建 RSA 密钥对，其中包含公钥
                .privateKey(this.priv) // 设置私钥，用于签名
                .build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk)); // 创建一个 JWK (JSON Web Key) 集合
        return new NimbusJwtEncoder(jwks); // 返回基于 Nimbus 的 JWT 编码器
    }
}
