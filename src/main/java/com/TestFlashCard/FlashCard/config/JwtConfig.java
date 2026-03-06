package com.TestFlashCard.FlashCard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String secret;

    private Long accessTokenExpiration;
    private Long renewalTokenExpiration;

    // Getters and setters
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(Long expiration) {
        this.accessTokenExpiration = expiration;
    }

    public void setRenewalTokenExpiration(Long expiration) {
        this.renewalTokenExpiration = expiration;
    }

    public Long getRenewalTokenExpiration() {
        return renewalTokenExpiration;
    }
}