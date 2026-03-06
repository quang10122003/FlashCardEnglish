package com.TestFlashCard.FlashCard.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.TestFlashCard.FlashCard.Enum.TokenError;
import com.TestFlashCard.FlashCard.config.JwtConfig;
import com.TestFlashCard.FlashCard.entity.User;

@Component
public class JwtTokenProvider {
    @Autowired
    private JwtConfig jwtConfig;

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getAccessTokenExpiration());

        // Chuyển đổi secret thành SecretKey
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(Integer.toString(user.getId()))
                .claim("role", user.getRole().name())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    public String generateRenewalToken(int Id) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getRenewalTokenExpiration());

        // Chuyển đổi secret thành SecretKey
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(Integer.toString(Id))
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public TokenValidationResult validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return new TokenValidationResult(true);
        }catch (ExpiredJwtException expiredJwtException){
            return new TokenValidationResult(false,TokenError.EXPIRED);
        }catch (MalformedJwtException malformedJwtException) {
            return new TokenValidationResult(false,TokenError.MALFORMED);
        }catch (IllegalArgumentException illegalArgumentException){
            return new TokenValidationResult(false,TokenError.ILLEGAL_ARGUMENT);
        }catch (UnsupportedJwtException unsupportedJwtException){
            return new TokenValidationResult(false,TokenError.UNSUPPORTED);
        }catch (Exception exception){
            return new TokenValidationResult(false, TokenError.UNKNOWN);
        }
    }

    public Integer getUserIdFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Integer.parseInt(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }
}