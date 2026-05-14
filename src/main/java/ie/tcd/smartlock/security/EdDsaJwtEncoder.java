package ie.tcd.smartlock.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.jwt.*;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.interfaces.EdECPrivateKey;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Ed25519 JwtEncoder——直接对着 RFC 7519 (JWT) + RFC 7515 (JWS Compact) 编程，
 * 不依赖 com.nimbusds.*，加密走 JDK 自带的 java.security.Signature("Ed25519")。
 * <p>
 * Token 结构：base64url(header_json) + "." + base64url(payload_json) + "." + base64url(Ed25519 signature)
 */
public class EdDsaJwtEncoder implements JwtEncoder {
    // Base64 编码器
    private static final Base64.Encoder B64URL = Base64.getUrlEncoder().withoutPadding();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    // iat / exp / nbf 必须是 NumericDate（自 epoch 起的秒数）
    private static final Set<String> NUMERIC_DATE_CLAIMS = Set.of("iat", "exp");
    // 单密钥固定 header，预先 base64url 化避免每次签名都重新 JSON 序列化
    private static final Map<String, Object> HEADER = Map.of("alg", "EdDSA", "typ", "JWT");
    private static final String HEADER_SEGMENT;

    static {
        try {
            // 编码 JWT 头
            //                              Base64 <- byte[] <- JSON <- Java Map
            //                              ASCII  <- UTF-8  <- UTF-16
            HEADER_SEGMENT = B64URL.encodeToString(MAPPER.writeValueAsBytes(HEADER));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize JWS header", e);
        }
    }

    private final EdECPrivateKey privateKey;

    public EdDsaJwtEncoder(EdECPrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public Jwt encode(JwtEncoderParameters parameters) throws JwtEncodingException {
        JwtClaimsSet claims = parameters.getClaims();

        // 把 Spring 的 Instant 时间型 claim 转成 epoch second
        Map<String, Object> payload = new LinkedHashMap<>(claims.getClaims());
        payload.replaceAll((k, v) ->
                NUMERIC_DATE_CLAIMS.contains(k) && v instanceof Instant inst ? inst.getEpochSecond() : v);

        String payloadSegment;
        try {
            // 编码 JWT 体
            payloadSegment = B64URL.encodeToString(MAPPER.writeValueAsBytes(payload));
        } catch (Exception e) {
            throw new JwtEncodingException("Failed to serialize JWT claims", e);
        }

        String signingInput = HEADER_SEGMENT + "." + payloadSegment;
        byte[] signature;
        try {
            Signature sig = Signature.getInstance("Ed25519");
            sig.initSign(privateKey);
            // 不用 UTF-8 是因为 Base64 不超过 US_ASCII 字符集
            sig.update(signingInput.getBytes(StandardCharsets.US_ASCII));
            // 执行签名
            signature = sig.sign();
        } catch (GeneralSecurityException e) {
            throw new JwtEncodingException("Ed25519 signing failed", e);
        }

        String token = signingInput + "." + B64URL.encodeToString(signature);
        return new Jwt(token, claims.getIssuedAt(), claims.getExpiresAt(), HEADER, claims.getClaims());
    }
}
