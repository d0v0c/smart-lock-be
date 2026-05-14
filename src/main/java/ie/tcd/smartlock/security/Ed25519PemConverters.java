package ie.tcd.smartlock.security;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.EdECPrivateKey;
import java.security.interfaces.EdECPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Ed25519 PEM 密钥文件 → Java 类的转换器。
 * <p>
 * 注册为 {@link ConfigurationPropertiesBinding} 后，Spring Boot 在绑定
 * {@code @ConfigurationProperties} 时会自动把 {@code classpath:app.key} /
 * {@code classpath:app.pub} 一路转成 {@link EdECPrivateKey} / {@link EdECPublicKey}。
 * 设计对标 Spring Boot 自带的 {@code RsaKeyConverters}。
 * <p>
 * String -> EdECPrivateKey
 * String -> EdECPublicKey
 */
public final class Ed25519PemConverters {

    private static final ResourceLoader LOADER = new DefaultResourceLoader();

    private Ed25519PemConverters() {
    }

    /**
     * PKCS#8 PEM → Ed25519 私钥。
     */
    @Component
    @ConfigurationPropertiesBinding
    public static class PrivateKeyConverter implements Converter<String, EdECPrivateKey> {
        @Override
        public EdECPrivateKey convert(String location) {
            byte[] der = decodePem(location, "PRIVATE KEY");
            try {
                return (EdECPrivateKey) KeyFactory.getInstance("Ed25519")
                        .generatePrivate(new PKCS8EncodedKeySpec(der));
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException("Failed to load Ed25519 private key from " + location, e);
            }
        }
    }

    /**
     * X.509 SubjectPublicKeyInfo PEM → Ed25519 公钥。
     */
    @Component
    @ConfigurationPropertiesBinding
    public static class PublicKeyConverter implements Converter<String, EdECPublicKey> {
        @Override
        public EdECPublicKey convert(String location) {
            byte[] der = decodePem(location, "PUBLIC KEY");
            try {
                return (EdECPublicKey) KeyFactory.getInstance("Ed25519")
                        .generatePublic(new X509EncodedKeySpec(der));
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException("Failed to load Ed25519 public key from " + location, e);
            }
        }
    }

    /**
     * 解析 {@code classpath:/file:} 等前缀的位置串，读 PEM，去信封，base64 解码成 DER。
     */
    private static byte[] decodePem(String location, String label) {
        Resource resource = LOADER.getResource(location);
        String pem;
        try {
            pem = resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read PEM from " + location, e);
        }
        String body = pem
                .replace("-----BEGIN " + label + "-----", "")
                .replace("-----END " + label + "-----", "")
                .replaceAll("\\s+", "");
        return Base64.getDecoder().decode(body);
    }
}
