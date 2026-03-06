package com.TestFlashCard.FlashCard.exception;

public class ResourceExistedException extends RuntimeException  {
    public ResourceExistedException(String message){
        super(message);
    }
}
