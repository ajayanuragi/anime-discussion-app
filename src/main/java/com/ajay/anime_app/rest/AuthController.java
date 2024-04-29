package com.ajay.anime_app.rest;

import com.ajay.anime_app.domain.AuthenticationResponse;
import com.ajay.anime_app.model.UserDTO;
import com.ajay.anime_app.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody @Valid UserDTO request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody UserDTO request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @GetMapping("/hello")
    public String hello() {
        return "HEllo";
    }

}
