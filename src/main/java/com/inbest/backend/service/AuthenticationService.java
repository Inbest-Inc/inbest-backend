package com.inbest.backend.service;

import com.inbest.backend.authentication.AuthenticationRequest;
import com.inbest.backend.authentication.AuthenticationResponse;
import com.inbest.backend.authentication.RegisterRequest;
import com.inbest.backend.exception.UserNotFoundException;
import com.inbest.backend.model.Role;
import com.inbest.backend.model.TokenType;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.UserRepository;
import com.inbest.backend.service.JwtService; // Bean olarak oluşturulduğu için kullanilmamis gibi gozukuyor
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService
{
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final EmailService emailService;

    public AuthenticationResponse register(RegisterRequest request)
    {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (repository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (repository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .surname(request.getSurname())
                .dateJoined(LocalDateTime.now())
                .isVerified(false)
                .isEnabled(false)
                .imageUrl("https://inbest-bucket.s3.eu-central-1.amazonaws.com/default.svg")
                .role(Role.USER)  // Default role ADMIN will be added from database manually
                .build();

        repository.save(user);

        String token = tokenService.createToken(user, TokenType.EMAIL_VERIFICATION, 30);

        String verificationLink = "http://tryinbest.com/verify?token=" + token;

        emailService.sendWelcomeEmail(user.getEmail(), verificationLink);

        return AuthenticationResponse.builder().build();
    }

    public boolean verifyEmail(String token)
    {
        var optionalToken = tokenService.validateToken(token, TokenType.EMAIL_VERIFICATION);

        if (optionalToken.isEmpty())
        {
            return false;
        }

        var verificationToken = optionalToken.get();
        User user = verificationToken.getUser();
        user.setVerified(true);
        user.setEnabled(true);
        repository.save(user);

        tokenService.invalidateToken(token);
        return true;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request)
    {
        var user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Invalid username or password"));

        if (!user.isVerified())
        {
            throw new IllegalStateException("Please verify your email before logging in.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    public int authenticate_user()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String token = (String) authentication.getCredentials();

        return jwtService.extractUserIdFromToken(token);
    }

    public String authenticateUsername()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String token = (String) authentication.getCredentials();
        return jwtService.extractUsername(token);
    }
}


