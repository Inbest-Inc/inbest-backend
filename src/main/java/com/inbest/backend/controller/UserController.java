package com.inbest.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/register")
    public ResponseEntity<String> getUser() {
        return ResponseEntity.status(HttpStatus.OK).body("User is returned successfully!");
    }
}
