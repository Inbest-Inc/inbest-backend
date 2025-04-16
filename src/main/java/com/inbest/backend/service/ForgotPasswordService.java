package com.inbest.backend.service;

import com.inbest.backend.model.TokenType;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.TokenRepository;
import com.inbest.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ForgotPasswordService
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TokenService tokenService;

    private static final int EXPIRATION_TIME = 30; // 30 min

    public void sendForgotPasswordEmail(String email)
    {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String resetToken = tokenService.createToken(user, TokenType.PASSWORD_RESET, EXPIRATION_TIME);
        String resetLink = "https://tryinbest.com/reset-password?token=" + resetToken;

        emailService.sendForgotPasswordEmail(user.getEmail(), user.getName(), resetLink);
    }
}
