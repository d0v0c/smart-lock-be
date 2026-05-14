package ie.tcd.smartlock.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.interfaces.EdECPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Ed25519 JwtDecoder——直接拆 JWS Compact 串、Jackson 解 JSON、JCA 验签。
 */
public class EdDsaJwtDecoder implements JwtDecoder {

    private static final Base64.Decoder B64URL = Base64.getUrlDecoder();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final Set<String> NUMERIC_DATE_CLAIMS = Set.of("iat", "exp");

    private final EdECPublicKey publicKey;
    private OAuth2TokenValidator<Jwt> validator = JwtValidators.createDefault();

    public EdDsaJwtDecoder(EdECPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void setJwtValidator(OAuth2TokenValidator<Jwt> validator) {
        this.validator = validator;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        // 1) 拆 token
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) {
            throw new BadJwtException("Malformed JWT: expected 3 segments, got " + parts.length);
        }

        // 2) 解 header，校验 alg（防 alg=none）
        Map<String, Object> headers = readJson(parts[0], "header");
        if (!"EdDSA".equals(headers.get("alg"))) {
            throw new BadJwtException("Unsupported JWS alg: " + headers.get("alg"));
        }

        // 3) 验签：注意签名输入用的是 token 里原样的 base64url 字符串，不能重新序列化
        byte[] signature;
        try {
            signature = B64URL.decode(parts[2]);
        } catch (IllegalArgumentException e) {
            throw new BadJwtException("Malformed JWT signature segment", e);
        }
        String signingInput = parts[0] + "." + parts[1];
        boolean ok;
        try {
            Signature sig = Signature.getInstance("Ed25519");
            sig.initVerify(publicKey);
            sig.update(signingInput.getBytes(StandardCharsets.US_ASCII));
            ok = sig.verify(signature);
        } catch (GeneralSecurityException e) {
            throw new BadJwtException("Ed25519 verification error", e);
        }
        if (!ok) {
            throw new BadJwtException("JWT signature mismatch");
        }

        // 4) 解 claims，把 NumericDate 转回 Instant
        Map<String, Object> rawClaims = readJson(parts[1], "claims");
        Map<String, Object> claims = new LinkedHashMap<>(rawClaims);
        claims.replaceAll((k, v) -> NUMERIC_DATE_CLAIMS.contains(k) && v instanceof Number n ? Instant.ofEpochSecond(n.longValue()) : v);

        Instant iat = claims.get("iat") instanceof Instant i ? i : null;
        Instant exp = claims.get("exp") instanceof Instant i ? i : null;
        Jwt jwt = new Jwt(token, iat, exp, headers, claims);

        // 5) 跑业务校验器（iat/exp/typ 等）
        OAuth2TokenValidatorResult result = validator.validate(jwt);
        if (result.hasErrors()) {
            throw new JwtValidationException("JWT validation failed", result.getErrors());
        }
        return jwt;
    }

    private static Map<String, Object> readJson(String segment, String what) {
        try {
            return MAPPER.readValue(B64URL.decode(segment), MAP_TYPE);
        } catch (Exception e) {
            throw new BadJwtException("Malformed JWT " + what, e);
        }
    }
}
