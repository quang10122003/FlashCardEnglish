package com.TestFlashCard.FlashCard.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.TestFlashCard.FlashCard.Enum.Role;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserResponse {
    private int id;
    private String fullName;
    private LocalDate birthday;
    private String email;
    private String avatar;
    private String phoneNumber;
    private String address;
    private String accountName;
    private String passWord;
    private LocalDateTime createAt;
    private Role role;
}
