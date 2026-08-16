package dev.incidentai.backend.dto;
import dev.incidentai.backend.entity.Environment;
import jakarta.validation.constraints.*;
public record ApplicationRequest(@NotBlank @Size(max=120) String name, @NotBlank @Size(max=500) @Pattern(regexp="https?://.+", message="deve iniciar com http:// ou https://") String url, @NotNull Environment environment, Boolean monitoringEnabled) {}
