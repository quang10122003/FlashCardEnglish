package com.TestFlashCard.FlashCard.config;

import org.springframework.http.HttpStatus;

import com.TestFlashCard.FlashCard.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

public class ApiResponseWriter {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void write(HttpServletResponse resp, HttpStatus status, String message, Object data) throws IOException {
        resp.setStatus(status.value());
        resp.setContentType("application/json;charset=UTF-8");
        ApiResponse<Object> body = ApiResponse.of(status.value(), message, data);
        MAPPER.writeValue(resp.getWriter(), body);
    }
}