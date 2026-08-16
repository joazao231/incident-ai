package dev.incidentai.backend.dto;
import dev.incidentai.backend.entity.*;
import java.time.LocalDateTime;
public record EventResponse(Long id,Long applicationId,EventType type,String message,Integer statusCode,Long responseTimeMs,LocalDateTime occurredAt){public static EventResponse from(MonitoringEvent e){return new EventResponse(e.getId(),e.getApplication().getId(),e.getType(),e.getMessage(),e.getStatusCode(),e.getResponseTimeMs(),e.getOccurredAt());}}
