package com.inbest.backend.service;

import com.inbest.backend.authentication.AuthenticationRequest;
import com.inbest.backend.authentication.AuthenticationResponse;
import com.inbest.backend.authentication.RegisterRequest;
import com.inbest.backend.exception.UserNotFoundException;
import com.inbest.backend.model.Role;
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

    public AuthenticationResponse register(RegisterRequest request)
    {
        if (repository.existsByUsername(request.getUsername()))
        {
            throw new IllegalArgumentException("Username already exists");
        }

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .surname(request.getSurname())
                .dateJoined(LocalDateTime.now())
                .role(Role.USER)  // Default role ADMIN will be added from database manually
                .build();

        repository.save(user);

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request)
    {
        var user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Invalid username or password"));

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


