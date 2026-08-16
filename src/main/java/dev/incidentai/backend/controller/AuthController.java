package dev.incidentai.backend.controller;

import dev.incidentai.backend.dto.*;
import dev.incidentai.backend.security.UserPrincipal;
import dev.incidentai.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) { return auth.login(request); }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) { return UserResponse.from(principal.user()); }
}
