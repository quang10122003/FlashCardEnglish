package com.TestFlashCard.FlashCard.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class UserUpdateProfileRequest {

    @Size(min = 1, max = 50)
    private String accountName;

    @Size (min = 1, max = 100)
    private String fullName;

    @Past(message = "Birth date must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @Email (message = "Email should be valid")
    private String email;

    private String address;
    private String phoneNumber;
}
