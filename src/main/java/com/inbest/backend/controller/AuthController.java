package com.inbest.backend.controller;

import com.inbest.backend.authentication.AuthenticationRequest;
import com.inbest.backend.authentication.AuthenticationResponse;
import com.inbest.backend.authentication.RegisterRequest;
import com.inbest.backend.model.response.GenericResponse;
import com.inbest.backend.service.AuthenticationService;
import jakarta.validation.Valid;
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
    public ResponseEntity<GenericResponse> register(@Valid @RequestBody RegisterRequest request)
    {
        AuthenticationResponse authenticationResponse = service.register(request);
        GenericResponse response = new GenericResponse(
                "success",
                "Registration successful. Please check your email to verify your account.",
                authenticationResponse
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<GenericResponse> authenticate(@Valid @RequestBody AuthenticationRequest request)
    {
        AuthenticationResponse authenticationResponse = service.authenticate(request);

        GenericResponse response = new GenericResponse(
                "success",
                "Authentication successful",
                authenticationResponse
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<GenericResponse> verifyEmail(@RequestParam("token") String token)
    {
        boolean isVerified = service.verifyEmail(token);

        GenericResponse response;
        if (isVerified)
        {
            response = new GenericResponse(
                    "success",
                    "Email verified successfully. You can log in.",
                    null
            );
            return ResponseEntity.ok(response);
        }
        else
        {
            response = new GenericResponse(
                    "error",
                    "Invalid or expired verification token.",
                    null
            );
            return ResponseEntity.status(400).body(response);
        }
    }
}
