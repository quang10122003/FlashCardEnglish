package com.TestFlashCard.FlashCard.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.repository.IUser_Repository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private final IUser_Repository userRepository;

    @Override
    public UserDetails loadUserByUsername(String accountName) throws UsernameNotFoundException {
        User user = userRepository.findByAccountName(accountName);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getAccountName())
                .password(user.getPassWord())
                .roles(user.getRole().name())
                .build();
    }

    // Thêm phương thức load user bằng ID (dùng cho JWT filter)
    public UserDetails loadUserById(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        return new org.springframework.security.core.userdetails.User(
                user.getAccountName(),
                user.getPassWord(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
