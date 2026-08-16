package dev.incidentai.backend.dto;

import dev.incidentai.backend.entity.*;
import java.time.LocalDateTime;

public record UserResponse(Long id, String name, String username, UserRole role, boolean enabled, LocalDateTime createdAt) {
    public static UserResponse from(UserAccount user) {
        return new UserResponse(user.getId(), user.getName(), user.getUsername(), user.getRole(), user.isEnabled(), user.getCreatedAt());
    }
}
