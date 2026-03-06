package com.TestFlashCard.FlashCard.exception;

import java.io.IOException;
import java.security.SignatureException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.authentication.*;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        String message = "Unauthorized";
        String detail= "AUTH_INVALID";
        Map<String, Object> data = new LinkedHashMap<>();
        Throwable cause = authException.getCause();

        if(authException instanceof InsufficientAuthenticationException){
            message = "Missing Token";
            detail = "AUTH_MISSING";
        } else if (authException instanceof CredentialsExpiredException || cause instanceof ExpiredJwtException) {
            message = "Token expired";
            detail = "AUTH_EXPIRED";
            if (cause instanceof ExpiredJwtException eje && eje.getClaims() != null) {
                data.put("expiredAt", eje.getClaims().getExpiration());
                data.put("subject", eje.getClaims().getSubject());
            }
        } else if(authException instanceof BadCredentialsException) {
            if(cause instanceof SignatureException) {
                message = "Invalid signature";
                detail = "AUTH_BAD_SIGNATURE";
            } else {
                message = "Malformed token";
                detail = "AUTH_MALFORMED";
            }
        }
    }
}
