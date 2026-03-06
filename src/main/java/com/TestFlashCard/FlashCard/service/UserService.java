package com.TestFlashCard.FlashCard.service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.TestFlashCard.FlashCard.Enum.Role;
import com.TestFlashCard.FlashCard.JpaSpec.UserSpecification;
import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.IUser_Repository;
import com.TestFlashCard.FlashCard.response.UserResponse;

import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    @Autowired
    private final IUser_Repository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MinIO_MediaService minIO_MediaService;

    public void createUser(User user) throws IOException{
        user.setPassWord(passwordEncoder.encode(user.getPassWord()));
        userRepository.save(user);
    }

    public boolean checkExistedAccountName(String accountName) {
        return userRepository.findByAccountName(accountName) != null;
    }

    public boolean checkExistedEmail(String email) {
        return userRepository.findByEmail(email) != null;
    }

    public void updateUser(User user) {
        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw e;
        }
    }

    public void deleteUser(int id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find user with id : " + id));
        user.setDeleted(true);
        userRepository.save(user);
    }

    public List<UserResponse> getAllUsers() {
        Specification<User> specification = Specification
                .where(UserSpecification.hasStatus(false).and(UserSpecification.hasRole(Role.USER)));
        List<User> users = userRepository.findAll(specification);
        return users.stream().map(this::convertToResponse).toList();
    }

    public User getUserByAccountName(String accountName) throws UsernameNotFoundException {
        return userRepository.findByAccountName(accountName);
    }

    public User getUserById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return user;
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public UserResponse convertToResponse(User user){
        String avatar = null;
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            avatar = minIO_MediaService.getPresignedURL(user.getAvatar(), Duration.ofDays(1));
        }
        UserResponse response = new UserResponse();
        response.setAccountName(user.getAccountName());
        response.setAddress(user.getAddress());
        response.setBirthday(user.getBirthday());
        response.setAvatar(avatar);
        response.setCreateAt(user.getCreateAt());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setId(user.getId());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRole(user.getRole());
        return response;
    }
    public User getCurrentUser() throws Exception {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            String username;
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }

            return userRepository.findByAccountName(username);
        } catch (Exception e) {
            throw new Exception("Lỗi khi lấy user hiện tại từ SecurityContextHolder: " + e);
        }
    }
}
