package com.inbest.backend.controller;

import com.inbest.backend.authentication.AuthenticationRequest;
import com.inbest.backend.authentication.AuthenticationResponse;
import com.inbest.backend.authentication.RegisterRequest;
import com.inbest.backend.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController
{

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request)
    {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request)
    {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token)
    {
        boolean isVerified = service.verifyEmail(token);

        if (isVerified)
        {
            return ResponseEntity.ok("Email verified successfully. You can log in.");
        }
        else
        {
            return ResponseEntity.status(400).body("Invalid or expired verification token.");
        }
    }
}
