package ie.tcd.smartlock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;


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
            RSAPrivateKey privateKey
    ) {
    }

    public record FileConfig(
            String dir
    ) {
    }
}
