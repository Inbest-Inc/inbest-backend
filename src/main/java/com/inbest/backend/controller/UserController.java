package com.inbest.backend.controller;

import com.inbest.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
