package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

import com.TestFlashCard.FlashCard.Enum.Role;

@Data
@Entity
@Table (name = "user")
@NoArgsConstructor
@AllArgsConstructor
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "fullName", nullable = false, length = 100)
    private String fullName;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "avatar")
    private String avatar;

    @Column(name =  "phoneNumber")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "accountName", nullable = false, unique = true, length = 50)
    private String accountName;

    @Column(name = "passWord", nullable = false)
    private String passWord;

    @Column(name = "createAt", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createAt;

    @Column(name = "isDeleted", nullable = false)
    private boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, columnDefinition = "ENUM('USER', 'ADMIN') DEFAULT 'USER'")
    private Role role;
}