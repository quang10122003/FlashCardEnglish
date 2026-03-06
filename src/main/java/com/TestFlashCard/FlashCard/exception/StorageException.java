package com.TestFlashCard.FlashCard.exception;

public class StorageException extends FileUploadException {
    public StorageException(String message) {
        super(message);
    }
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}