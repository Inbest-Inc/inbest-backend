package com.inbest.backend.service;

import com.inbest.backend.authentication.ResetPasswordRequest;
import com.inbest.backend.model.Role;
import com.inbest.backend.model.Token;
import com.inbest.backend.model.TokenType;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ResetPasswordService
{
    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    public void resetPassword(String token, ResetPasswordRequest password)
    {
        Token resetToken = tokenService.validateToken(token, TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        User user = resetToken.getUser();

        user.setPasswordHash(passwordEncoder.encode(password.getPassword()));

        userRepository.save(user);

        tokenService.invalidateToken(token);
    }
}
