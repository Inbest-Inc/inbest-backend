package com.inbest.backend.controller;

import com.inbest.backend.dto.DeleteAccountRequest;
import com.inbest.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    @GetMapping("/{username}")
    public ResponseEntity<?> getPublicUserInfo(@PathVariable String username) {
        return ResponseEntity.ok(Map.of("name", userService.getPublicUserInfo(username)));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(@RequestBody @Valid DeleteAccountRequest request) {
        userService.deleteAccount(request.getPassword());
        return ResponseEntity.ok("Account deleted successfully");
    }

}
