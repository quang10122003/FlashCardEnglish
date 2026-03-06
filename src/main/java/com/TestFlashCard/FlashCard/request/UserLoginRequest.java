package com.TestFlashCard.FlashCard.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserLoginRequest {
    @NotBlank
    @NotEmpty
    @NotNull
    public String accountName;

    @NotBlank
    @NotEmpty
    @NotNull
    public String passWord;
}
