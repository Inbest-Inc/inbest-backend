package com.inbest.backend.service;

import com.inbest.backend.dto.UserUpdateDTO;
import com.inbest.backend.exception.UserNotFoundException;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.UserRepository;
import com.inbest.backend.dto.ChangePasswordDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public String getPublicUserInfo(String username) {
        Optional<User> user = repository.findByUsername(username);
        return user.map(value -> value.getName() + " " + value.getSurname()).orElse("John Doe");
    }

    public void updateUserNameAndSurname(UserUpdateDTO userUpdateDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = repository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setName(userUpdateDTO.getName());
        user.setSurname(userUpdateDTO.getSurname());

        repository.save(user);
    }

    public void changePassword(ChangePasswordDTO request) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = repository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Check if current password matches
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password not correct");
        }

        // Check if new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New passwords do not match");
        }

        // Check if new password is the same as current password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("New password can not be the same as current password");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
    }

    public List<User> searchUsers(String searchTerm) {
        return repository.searchUsers(searchTerm);
    }
}