package com.inbest.backend.controller;

import com.inbest.backend.dto.UserDTO;
import com.inbest.backend.dto.UserUpdateDTO;
import com.inbest.backend.exception.UserNotFoundException;
import com.inbest.backend.service.AuthenticationService;
import com.inbest.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Valid;

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

    @PutMapping("/update")
    public ResponseEntity<?> updateUserNameAndSurname(@Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        try {
            userService.updateUserNameAndSurname(userUpdateDTO);

            return ResponseEntity.ok(Map.of(
                    "message", "User information updated successfully"
            ));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while updating user information"));
        }
    }
}
