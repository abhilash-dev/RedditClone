package com.abhilash.redditclone.controller;

import com.abhilash.redditclone.dto.RegisterRequest;
import com.abhilash.redditclone.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> signup(@RequestBody RegisterRequest registerRequest) {
        authService.signup(registerRequest);
        return new ResponseEntity<>(registerRequest.getUsername() + " registered successfully", HttpStatus.OK);
    }

    @GetMapping("/verify/{token}")
    public ResponseEntity<String> verify(@PathVariable String token) {
        authService.verifyAndEnableUser(token);
        return new ResponseEntity<>("User verified and enabled successfully", HttpStatus.OK);
    }
}
