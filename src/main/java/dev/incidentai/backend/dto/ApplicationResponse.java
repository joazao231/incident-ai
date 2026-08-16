package dev.incidentai.backend.dto;
import dev.incidentai.backend.entity.*;
import java.time.LocalDateTime;
public record ApplicationResponse(Long id,String name,String url,Environment environment,boolean monitoringEnabled,ApplicationStatus status,Integer lastStatusCode,Long lastResponseTimeMs,LocalDateTime lastCheckedAt,String lastError,LocalDateTime createdAt,LocalDateTime updatedAt) {
 public static ApplicationResponse from(MonitoredApplication a){return new ApplicationResponse(a.getId(),a.getName(),a.getUrl(),a.getEnvironment(),a.isMonitoringEnabled(),a.getStatus(),a.getLastStatusCode(),a.getLastResponseTimeMs(),a.getLastCheckedAt(),a.getLastError(),a.getCreatedAt(),a.getUpdatedAt());}
}
