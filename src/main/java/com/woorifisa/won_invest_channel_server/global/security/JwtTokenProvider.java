package com.woorifisa.won_invest_channel_server.global.security;

import com.woorifisa.won_invest_channel_server.domain.auth.exception.code.AuthErrorCode;
import com.woorifisa.won_invest_channel_server.global.config.SecurityProperties;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long accessTokenExpirationSeconds;

    public JwtTokenProvider(SecurityProperties securityProperties) {
        this.signingKey = Keys.hmacShaKeyFor(securityProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationSeconds = securityProperties.getAccessTokenExpirationSeconds();
    }

    public AuthenticatedUser parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String authUserUuid = claims.get("authUserUuid", String.class);
            String userUuid = claims.getSubject();
            String jti = claims.getId();

            return new AuthenticatedUser(
                    parseRequiredUuid(authUserUuid, "authUserUuid"),
                    parseRequiredUuid(userUuid, "subject"),
                    parseRequiredClaim(jti, "jti")
            );
        } catch (ExpiredJwtException e) {
            throw new BusinessException(AuthErrorCode.TOKEN_EXPIRED, e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN, e);
        }
    }

    public String generateAccessToken(UUID userUuid, UUID authUserUuid, String jti) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenExpirationSeconds);

        return Jwts.builder()
                .subject(userUuid.toString())
                .claim("authUserUuid", authUserUuid.toString())
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    private UUID parseRequiredUuid(String value, String claimName) {
        if (value == null || value.isBlank()) {
            throw new JwtException("Missing required JWT claim: " + claimName);
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new JwtException("Invalid UUID claim: " + claimName, e);
        }
    }

    private String parseRequiredClaim(String value, String claimName) {
        if (value == null || value.isBlank()) {
            throw new JwtException("Missing required JWT claim: " + claimName);
        }
        return value;
    }
}
