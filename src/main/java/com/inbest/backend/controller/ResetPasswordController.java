package com.inbest.backend.controller;

import com.inbest.backend.authentication.ResetPasswordRequest;
import com.inbest.backend.service.ResetPasswordService;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class ResetPasswordController
{
    @Autowired
    private ResetPasswordService resetPasswordService;

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestBody ResetPasswordRequest password) {
        try {
            resetPasswordService.resetPassword(token, password);
            return ResponseEntity.ok("You password is reset successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
