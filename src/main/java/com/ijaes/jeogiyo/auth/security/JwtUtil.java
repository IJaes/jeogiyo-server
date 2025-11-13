package com.ijaes.jeogiyo.auth.security;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        try {
            return getClaims(token).getSubject();
        } catch (ExpiredJwtException ex) {
            throw new CustomException(ErrorCode.JWT_EXPIRED);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new CustomException(ErrorCode.INVALID_JWT);
        }
    }

    public void validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException ex) {
            log.warn("JWT token expired: {}", ex.getMessage());
            throw new CustomException(ErrorCode.JWT_EXPIRED);
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token: {}", ex.getMessage());
            throw new CustomException(ErrorCode.UNSUPPORTED_JWT);
        } catch (MalformedJwtException ex) {
            log.warn("Malformed JWT token: {}", ex.getMessage());
            throw new CustomException(ErrorCode.MALFORMED_JWT);
        } catch (SecurityException ex) {
            log.warn("JWT signature verification failed: {}", ex.getMessage());
            throw new CustomException(ErrorCode.JWT_SIGNATURE_INVALID);
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
            throw new CustomException(ErrorCode.INVALID_JWT);
        } catch (JwtException ex) {
            log.warn("JWT exception: {}", ex.getMessage());
            throw new CustomException(ErrorCode.INVALID_JWT);
        }
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new CustomException(ErrorCode.JWT_EXPIRED);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new CustomException(ErrorCode.INVALID_JWT);
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return getClaims(token).getExpiration().before(new Date());
        } catch (CustomException ex) {
            return true;
        }
    }

    public long getTokenExpirationTime(String token) {
        try {
            return getClaims(token).getExpiration().getTime();
        } catch (CustomException ex) {
            throw ex;
        }
    }
}