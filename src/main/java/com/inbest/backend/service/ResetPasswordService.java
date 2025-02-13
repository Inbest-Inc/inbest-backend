package com.inbest.backend.service;

import com.inbest.backend.model.Token;
import com.inbest.backend.model.TokenType;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ResetPasswordService
{
    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    public void resetPassword(String token, String password)
    {
        Token resetToken = tokenService.validateToken(token, TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);

        tokenService.invalidateToken(token);
    }
}
