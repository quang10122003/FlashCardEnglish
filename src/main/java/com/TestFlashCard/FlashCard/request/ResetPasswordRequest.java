package com.TestFlashCard.FlashCard.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotNull (message = "old Password cannot be null")
    @NotEmpty(message = "old Password cannot be empty")
    @NotBlank(message = "old Password cannot be blank")
    private String oldPassword;
    @NotNull (message = "New Password cannot be null")
    @NotEmpty(message = "New Password cannot be empty")
    @NotBlank(message = "New Password cannot be blank")
    private String newPassword;
}
