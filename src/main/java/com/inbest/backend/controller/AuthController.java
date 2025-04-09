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

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController
{

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request)
    {
        try
        {
            service.register(request);

            return ResponseEntity.ok(Map.of(
                    "message", "Registration successful. Please check your email to verify your account.",
                    "status", "success"
            ));
        }
        catch (IllegalArgumentException e)
        {

            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage(),
                    "status", "error"
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request)
    {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam("token") String token)
    {
        boolean isVerified = service.verifyEmail(token);

        if (isVerified)
        {
            return ResponseEntity.ok(Map.of(
                    "message", "Email verified successfully. You can log in.",
                    "status", "success"
            ));
        }
        else
        {
            return ResponseEntity.status(400).body(Map.of(
                    "message", "Invalid or expired verification token.",
                    "status", "error"
            ));
        }
    }
}
