package ie.tcd.smartlock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;


@ConfigurationProperties(prefix = "smart-lock")
public record SmartLockProperties(
        Mqtt mqtt,
        Jwt jwt,
        FileConfig file
) {

    public record Mqtt(
            String username,
            String password,
            String url,
            String clientId,
            String certFile
    ) {
    }

    public record Jwt(
            RSAPublicKey publicKey,
            RSAPrivateKey privateKey,
            Duration accessTtl,
            Duration refreshTtl
    ) {
    }

    public record FileConfig(
            String dir
    ) {
    }
}
