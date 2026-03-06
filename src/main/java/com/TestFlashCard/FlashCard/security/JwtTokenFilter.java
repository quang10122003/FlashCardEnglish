package com.TestFlashCard.FlashCard.security;

import java.io.IOException;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.TestFlashCard.FlashCard.exception.TokenAuthenticationException;
import com.TestFlashCard.FlashCard.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private JwtTokenProvider tokenProvider;

    private CustomUserDetailsService userDetailsService;

    public JwtTokenFilter(JwtTokenProvider tokenProvider, CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/user/login",
            "/api/user/create",
            "/api/user/getUserByFilter",
            "/api/user/register",
            "/api/user/forgot-password",
            "/api/user/verify-reset-code",
            "/api/exam/getByCreateAt",
            "/api/flashcard/getTopicPopular",
            "/api/flashcard/id",
            "/api/flashcard/topic",
            "/api/flashcard/getFlashCardsByTopic",
            "/api/flashcard/raiseVisitCount",
            "/api/evaluate/get",
            "/api/blog/category/getAll",
            "/api/blog/id",
            "/api/blog/getAll",
            "/api/card/getByFlashCard",
            "/api/exam/filter",
            "/api/exam/collection/getAll",
            "/api/exam/comments",
            "/api/exam/detail/",
            "/api/payment/vnpay-return"
            );

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = getTokenFromRequest(request);
            if (token == null) {
                throw new TokenAuthenticationException("Token is null", "TOKEN_MISSING");
            }

            TokenValidationResult tokenValidationResult = tokenProvider.validateToken(token);
            if (!tokenValidationResult.isValid()) {
                throw new TokenAuthenticationException("Token validation failed!", tokenValidationResult.getErrorCode());
            }

            int userId = tokenProvider.getUserIdFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserById(userId);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            //
            //System.out.println(">> Authorities: " + userDetails.getAuthorities());

            // ✅ Sau khi xử lý thành công → tiếp tục filter
            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            // ✅ Tránh tiếp tục xử lý sau khi đã set lỗi
            ex.printStackTrace();
            throw new IOException("Could not set user authentication in security context");
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}