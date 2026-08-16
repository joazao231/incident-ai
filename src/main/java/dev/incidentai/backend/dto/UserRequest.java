package dev.incidentai.backend.dto;

import dev.incidentai.backend.entity.UserRole;
import jakarta.validation.constraints.*;

public record UserRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(min = 3, max = 80) @Pattern(regexp = "[a-zA-Z0-9._-]+", message = "use apenas letras, números, ponto, hífen ou sublinhado") String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull UserRole role
) {}
