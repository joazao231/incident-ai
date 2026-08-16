package dev.incidentai.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "monitoring_events", indexes = @Index(name="idx_event_app_time", columnList="application_id,occurredAt"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MonitoringEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private MonitoredApplication application;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private EventType type;
    @Column(nullable = false, length = 500) private String message;
    private Integer statusCode;
    private Long responseTimeMs;
    @Column(nullable = false, updatable = false) private LocalDateTime occurredAt;
    @PrePersist void onCreate() { if(occurredAt==null) occurredAt=LocalDateTime.now(); }
}
