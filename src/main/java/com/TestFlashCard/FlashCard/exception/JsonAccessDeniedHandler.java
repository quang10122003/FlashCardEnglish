package com.TestFlashCard.FlashCard.exception;

import java.io.IOException;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.TestFlashCard.FlashCard.config.ApiResponseWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JsonAccessDeniedHandler implements AccessDeniedHandler{
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, AccessDeniedException exception) throws IOException {
        ApiResponseWriter.write(resp, HttpStatus.FORBIDDEN, "Access Denied", Map.of("detail", "ACCESS DENIED"));
    }
}
