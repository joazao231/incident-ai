package dev.incidentai.backend.controller;

import dev.incidentai.backend.dto.*;
import dev.incidentai.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/users")
public class UserController {
    private final AuthService auth;
    public UserController(AuthService auth) { this.auth = auth; }

    @GetMapping public List<UserResponse> all() { return auth.findAll(); }
    @PostMapping public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.create(request));
    }
}
