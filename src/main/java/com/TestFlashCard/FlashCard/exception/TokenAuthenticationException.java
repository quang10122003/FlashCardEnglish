package com.TestFlashCard.FlashCard.exception;

public class TokenAuthenticationException extends RuntimeException {
    private final String errorCode;
    public TokenAuthenticationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    public String getErrorCode() {
        return errorCode;
    }
}
