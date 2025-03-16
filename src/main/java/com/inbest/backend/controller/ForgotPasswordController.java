package com.inbest.backend.controller;

import com.inbest.backend.service.ForgotPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class ForgotPasswordController
{
    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @PostMapping("/forgot-password")
    public  ResponseEntity<Map<String, String>> forgotPassword(@RequestParam String email) {
        Map<String, String> response = new HashMap<>();
        try {
            forgotPasswordService.sendForgotPasswordEmail(email);
            response.put("status", "success");
            response.put("message", "Password reset email has been sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
