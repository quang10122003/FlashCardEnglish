package com.TestFlashCard.FlashCard.security;

import com.TestFlashCard.FlashCard.Enum.TokenError;

public class TokenValidationResult {
    private boolean valid;
    private String errorCode;  // Mã lỗi (ví dụ: "EXPIRED", "INVALID_SIGNATURE")
    private String message;    // Thông báo chi tiết

    // Constructor cho token hợp lệ
    public TokenValidationResult(boolean valid) {
        this.valid = valid;
    }

    // Constructor cho token không hợp lệ
    public TokenValidationResult(boolean valid, TokenError error) {
    this.valid = valid;
    this.errorCode = error.getCode();
    this.message = error.getMessage();
}

    // Getters và Setters
    public boolean isValid() { return valid; }
    public String getErrorCode() { return errorCode; }
    public String getMessage() { return message; }
}