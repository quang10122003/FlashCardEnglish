package com.TestFlashCard.FlashCard.request;

import com.TestFlashCard.FlashCard.Enum.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserCreateRequest {
    @NotNull(message = "User's fullname cannot be null")
    @NotBlank(message = "User's fullname cannot be blank")
    @NotEmpty(message = "User's fullname cannot be empty")
    private String fullName = null;

    @NotNull(message = "User's email cannot be null")
    @NotBlank(message = "User's email cannot be blank")
    @NotEmpty(message = "User's email cannot be empty")
    @Email(message = "User's email must be valid")
    private String email = null;

    private String address;
    private String phoneNumber;

    @NotNull(message = "User's accountName cannot be null")
    @NotBlank(message = "User's accountName cannot be blank")
    @NotEmpty(message = "User's accountName cannot be empty")
    private String accountName = null;

    @NotNull(message = "User's password cannot be null")
    private String passWord = null;

    private Role role;
}
