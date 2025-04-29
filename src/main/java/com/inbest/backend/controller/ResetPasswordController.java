package com.inbest.backend.controller;

import com.inbest.backend.authentication.ResetPasswordRequest;
import com.inbest.backend.service.ResetPasswordService;
import jakarta.validation.Valid;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class ResetPasswordController
{
    @Autowired
    private ResetPasswordService resetPasswordService;

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>>  resetPassword(@RequestParam String token, @Valid @RequestBody ResetPasswordRequest password) {
        Map<String, String> response = new HashMap<>();
        try {
            resetPasswordService.resetPassword(token, password);
            response.put("status", "success");
            response.put("message", "Your password has been reset successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
