package com.inbest.backend.service;

import com.inbest.backend.model.User;
import com.inbest.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final UserRepository repository;

    public String getPublicUserInfo(String username) {
        Optional<User> user = repository.findByUsername(username);
        return user.map(value -> value.getName() + " " + value.getSurname()).orElse("John Doe");
    }
}
