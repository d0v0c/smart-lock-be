package ie.tcd.smartlock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.EdECPrivateKey;
import java.security.interfaces.EdECPublicKey;
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
            EdECPublicKey publicKey,
            EdECPrivateKey privateKey,
            Duration accessTtl,
            Duration refreshTtl
    ) {
    }

    public record FileConfig(
            String dir
    ) {
    }
}
