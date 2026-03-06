package com.TestFlashCard.FlashCard.Enum;

public enum TokenError {
    NULL("NULL","Token cannot be null"),
    EXPIRED("EXPIRED", "Token has expired"),
    INVALID_SIGNATURE("INVALID_SIGNATURE", "Invalid signature"),
    MALFORMED("MALFORMED", "Malformed token"),
    ILLEGAL_ARGUMENT("ILLEGAL_ARGUMENT","Illegal argument token"),
    UNSUPPORTED("UNSUPPORTED", "Unsupported token"),
    UNKNOWN("UNKNOW","Unknow error");

    private final String code;
    private final String message;

    TokenError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    // Getters
    public String getCode() { return code; }
    public String getMessage() { return message; }
}