package dev.incidentai.backend.dto;

import dev.incidentai.backend.entity.UserRole;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        Long userId,
        String name,
        String username,
        UserRole role
) {}
