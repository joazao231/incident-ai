package dev.incidentai.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "incidents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Incident {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private MonitoredApplication application;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, length = 1000) private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private IncidentSeverity severity;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private IncidentStatus status;
    @Column(nullable = false, updatable = false) private LocalDateTime openedAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime resolvedAt;
    @PrePersist void onCreate() { if(openedAt==null) openedAt=LocalDateTime.now(); if(status==null) status=IncidentStatus.OPEN; }
}
