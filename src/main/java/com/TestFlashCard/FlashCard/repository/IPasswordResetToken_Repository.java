package com.TestFlashCard.FlashCard.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.PasswordResetToken;

@Repository
public interface IPasswordResetToken_Repository extends JpaRepository <PasswordResetToken,Integer> {
    Optional<PasswordResetToken> findTopByEmailOrderByCreatedAtDesc(String email);
}
