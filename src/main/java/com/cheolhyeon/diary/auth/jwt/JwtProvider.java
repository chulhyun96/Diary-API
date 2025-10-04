package com.cheolhyeon.diary.auth.jwt;

import com.cheolhyeon.diary.app.properties.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private static final SecureRandom RNG = new SecureRandom();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Long userId, String sessionId) {
        Date now = new Date();
        Date expiryDate = new Date(getExpirationDate(now, "ACCESS"));
        return Jwts.builder()
                .issuer(jwtProperties.getIss())
                .audience().and()
                .claim("sessionOpt1", sessionId) // 로그인 세션
                .claim("sessionOpt2", userId) // 유저 PK
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateOpaqueRT() {
        byte[] randomBytes = new byte[jwtProperties.getRtLengthBytes()];
        RNG.nextBytes(randomBytes);
        return BASE64_ENCODER.encodeToString(randomBytes); // RT 원문
    }

    public String hashRT(String rtPlain) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        byte[] secretKey = Base64.getDecoder().decode(jwtProperties.getRtHmacSecret());
        mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
        byte[] h = mac.doFinal(rtPlain.getBytes(StandardCharsets.UTF_8));
        return BASE64_ENCODER.encodeToString(h); // DB 저장용 rt_hash
    }

    public long getExpirationDate(Date now, String tokenType) {
        if (tokenType.equals("REFRESH")) {
            return now.getTime() + jwtProperties.getRefreshTokenExpiration();
        }
        return now.getTime() + jwtProperties.getAccessTokenExpiration();
    }

    public String getSessionIdFromAccessToken(String token) {
        Claims payload = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return payload.get("sessionOpt1", String.class);
    }

    public Long getUserIdFromAccessToken(String token) {
        Claims payload = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return payload.get("sessionOpt2", Long.class);
    }

    public boolean validateAccessToken(String accessToken) {
        try {
            Claims body = Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
            String sid = body.get("sessionOpt1", String.class);
            if (sid == null || Objects.requireNonNull(sid).trim().isEmpty()) {
                log.warn("Invalid session id : {}", sid);
                return false;
            }
            Long userId = body.get("sessionOpt2", Long.class);
            if (userId == null || userId <= 0) {
                log.warn("Invalid user id : {}", userId);
                return false;
            }
        } catch (SignatureException e) {
            log.warn("Invalid signature : {}", e.getMessage());
            return false;
        } catch (ExpiredJwtException e) {
            log.warn("Expired token : {}", e.getMessage());
            return false;
        } catch (PrematureJwtException e) {
            log.warn("Premature token : {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported jwt : {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Malformed jwt : {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("Exception  : {}", e.getMessage());
            return false;
        }
        return true;
    }
}
