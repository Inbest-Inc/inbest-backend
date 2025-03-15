package com.inbest.backend.service;

import com.inbest.backend.dto.UserUpdateDTO;
import com.inbest.backend.exception.UserNotFoundException;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public String getPublicUserInfo(String username) {
        Optional<User> user = repository.findByUsername(username);
        return user.map(value -> value.getName() + " " + value.getSurname()).orElse("John Doe");
    }

    public void deleteAccount(String password){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = repository.findByUsername(username).orElseThrow(()-> new UserNotFoundException("User not found"));
        if (!passwordEncoder.matches(password,user.getPasswordHash())){
            throw new IllegalArgumentException("Incorrect password");
        }

        repository.delete(user);
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
}
