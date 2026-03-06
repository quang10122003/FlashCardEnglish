package com.TestFlashCard.FlashCard.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.TestFlashCard.FlashCard.exception.JsonAccessDeniedHandler;
import com.TestFlashCard.FlashCard.exception.JsonAuthenticationEntryPoint;
import com.TestFlashCard.FlashCard.security.JwtTokenFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class securityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/user/login").permitAll()
                        .requestMatchers("/api/user/forgot-password").permitAll()
                        .requestMatchers("/api/user/verify-reset-code").permitAll()
                        .requestMatchers("/api/user/register").permitAll()
                        .requestMatchers("/api/user/create").permitAll()
                        .requestMatchers("/api/user/getUserByFilter").permitAll()
                        .requestMatchers("/api/exam/getByCreateAt").permitAll()
                        .requestMatchers("/api/flashcard/getTopicPopular").permitAll()
                        .requestMatchers("/api/flashcard/id/**").permitAll()
                        .requestMatchers("/api/flashcard/getFlashCardsByTopic/**").permitAll()
                        .requestMatchers("/api/flashcard/topic/**").permitAll()
                        .requestMatchers("/api/flashcard/raiseVisitCount/**").permitAll()
                        .requestMatchers("/api/card/getByFlashCard/**").permitAll()
                        .requestMatchers("/api/evaluate/get").permitAll()
                        .requestMatchers("/api/blog/category/getAll").permitAll()
                        .requestMatchers("/api/blog/id/**").permitAll()
                        .requestMatchers("/api/blog/getAll").permitAll()
                        .requestMatchers("/api/exam/filter").permitAll()
                        .requestMatchers("/api/exam/collection/getAll").permitAll()
                        .requestMatchers("/api/payment/vnpay-return").permitAll()
                        .requestMatchers("/api/exam/comments/**").permitAll()
                        .requestMatchers("/api/exam/detail/**").permitAll()
                        .requestMatchers("/api/blog/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/user/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/dashboard/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/exam/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/evaluate/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(new JsonAuthenticationEntryPoint())
                    .accessDeniedHandler(new JsonAccessDeniedHandler())
                )
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
        
        System.out.println("---------hahahahha---------");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS","PATCH"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}