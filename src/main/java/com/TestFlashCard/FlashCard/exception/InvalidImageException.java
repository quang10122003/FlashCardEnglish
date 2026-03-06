package com.TestFlashCard.FlashCard.exception;

public class InvalidImageException extends FileUploadException {
    public InvalidImageException(String message) {
        super(message);
    }
    public InvalidImageException(String message, Throwable cause) {
        super(message, cause);
    }
}
