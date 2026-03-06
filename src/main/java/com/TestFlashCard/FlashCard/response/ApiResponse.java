package com.TestFlashCard.FlashCard.response;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;

    public ApiResponse() {}
    public ApiResponse(int status, String message, T data) {
        this.status = status; this.message = message; this.data = data;
    }
    public ApiResponse(int status, String message) {
        this.status = status; this.message = message;
    }

    public static <T> ApiResponse<T> of(int status, String message, T data) {
        return new ApiResponse<>(status, message, data);
    }
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Success", data);
    }
    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }
    public static ApiResponse<Map<String, String>> success(String detail) {
        return new ApiResponse<>(200, "Success", Collections.singletonMap("detail", detail));
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
