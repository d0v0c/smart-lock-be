//package ie.tcd.scss.smartdoorlockbe.utils;
//
//import io.jsonwebtoken.*;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import javax.xml.bind.DatatypeConverter;
//import java.time.Duration;
//import java.util.Date;
//import java.util.Map;
//
//@Component
//@Slf4j
//public class JwtUtil {
//    @Value("${jwt.secret-key}")
//    private String secretKey;
//    @Value("${jwt.exp-time}")
//    private Duration expTime;
//
//    /**
//     * 签发token
//     *
//     * @param subject: 存用户ID
//     * @param claims:  存内容
//     * @return
//     */
//    public String createToken(String subject, Map<String, Object> claims) {
//        JwtBuilder builder = Jwts.builder();
//        //负载信息
//        if (claims != null) {
//            builder.setClaims(claims);
//        }
//        //用户ID
//        if (StringUtils.hasLength(subject)) {
//            builder.setSubject(subject);
//        }
//        //签发时间
//        long currentMillis = System.currentTimeMillis();
//        builder.setIssuedAt(new Date(currentMillis));
//        //到期时间
//        long millis = expTime.toMillis();
//        if (millis > 0) {
//            builder.setExpiration(new Date(currentMillis + millis));
//        }
//        //签名密钥，不能公布
//        if (StringUtils.hasLength(secretKey)) {
//            builder.signWith(SignatureAlgorithm.HS256, DatatypeConverter.parseBase64Binary(secretKey));
//        }
//        return builder.compact();
//    }
//
//    /**
//     * 解析Token
//     *
//     * @param token
//     * @return
//     */
//    private Claims pareToken(String token) {
//        try {
//            Claims claims = Jwts.parser()
//                    .setSigningKey(DatatypeConverter.parseBase64Binary(secretKey))
//                    .parseClaimsJws(token)
//                    .getBody();
//            return claims;
//        } catch (ExpiredJwtException e) {
//            log.error("ExpiredJwtException:{}", e);
//        } catch (UnsupportedJwtException e) {
//            log.error("UnsupportedJwtException:{}", e);
//        } catch (MalformedJwtException e) {
//            log.error("MalformedJwtException:{}", e);
//        } catch (SignatureException e) {
//            log.error("SignatureException:{}", e);
//        } catch (IllegalArgumentException e) {
//            log.error("IllegalArgumentException:{}", e);
//        }
//        return null;
//    }
//
//    /**
//     * 获取用户ID
//     *
//     * @param token
//     * @return
//     */
//    public String getUserId(String token) {
//        Claims claims = pareToken(token);
//        if (claims != null) {
//            return claims.getSubject();
//        }
//        return null;
//    }
//
//    /**
//     * 校验token
//     *
//     * @param token
//     * @return True:校验通过
//     */
//    public boolean validateToken(String token) {
//        try {
//            Claims claims = pareToken(token);
//            Date exp = claims.getExpiration();
//            return exp.after(new Date());
//        } catch (Exception e) {
//            log.error("validateToken:{}", e);
//            return false;
//        }
//    }
//}
//
