package dev.incidentai.backend.dto;
import dev.incidentai.backend.entity.IncidentSeverity;
import jakarta.validation.constraints.*;
public record IncidentRequest(@NotNull Long applicationId,@NotBlank @Size(max=160) String title,@NotBlank @Size(max=1000) String description,@NotNull IncidentSeverity severity) {}
