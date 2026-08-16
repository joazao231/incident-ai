package dev.incidentai.backend.dto;
import dev.incidentai.backend.entity.*;
import java.time.LocalDateTime;
public record IncidentResponse(Long id,Long applicationId,String applicationName,String title,String description,IncidentSeverity severity,IncidentStatus status,LocalDateTime openedAt,LocalDateTime acknowledgedAt,LocalDateTime resolvedAt){public static IncidentResponse from(Incident i){return new IncidentResponse(i.getId(),i.getApplication().getId(),i.getApplication().getName(),i.getTitle(),i.getDescription(),i.getSeverity(),i.getStatus(),i.getOpenedAt(),i.getAcknowledgedAt(),i.getResolvedAt());}}
