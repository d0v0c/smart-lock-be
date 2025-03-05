package ie.tcd.scss.smartdoorlockbe.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * @author xylingying
 * @date 2025-03-05 21:29
 * @description: Spring Security 配置类
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
                        .anyRequest().authenticated() // 任何请求都需要认证
                )
                .csrf((csrf) -> csrf.ignoringRequestMatchers("/api/user/token")) // 禁用 /token 端点上的 CSRF 防护
                .httpBasic(Customizer.withDefaults()) // 启用基本认证，默认配置
//                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt) // 配置资源服务器为 JWT 模式
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())))
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 设置会话管理策略为无状态（REST API 推荐无状态，以支持水平扩展）
                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()) // 设置认证入口点，处理未认证的请求
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler()) // 设置拒绝访问处理器，处理权限不足的请求
                );
        // @formatter:on
        return http.build(); // 构建并返回过滤链
    }

    @Bean
    UserDetailsService users() {
        // 定义一个内存中的用户存储，以用于演示，生产环境推荐使用数据库或其他存储方式
        // @formatter:off
        return new InMemoryUserDetailsManager(
                User.withUsername("user") // 创建一个用户名为 "user" 的测试用户
                        .password("{noop}password") // 定义密码为 "password"，并指定 "{noop}" 表示不进行加密
                        .authorities("app") // 设置该用户的权限为 "app"
                        .build() // 构建用户对象
        );
        // @formatter:on
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
