package com.TestFlashCard.FlashCard.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ForgetPasswordRequest {
    @NotBlank
    @NotNull
    @Email
    private String email;
    private String accountName;
}
