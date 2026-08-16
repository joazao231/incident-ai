package dev.incidentai.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "monitored_applications", uniqueConstraints = @UniqueConstraint(name = "uk_application_url", columnNames = "url"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MonitoredApplication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, length = 500) private String url;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Environment environment;
    @Column(nullable = false) private boolean monitoringEnabled;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ApplicationStatus status;
    private Integer lastStatusCode;
    private Long lastResponseTimeMs;
    private LocalDateTime lastCheckedAt;
    @Column(length = 500) private String lastError;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private LocalDateTime updatedAt;
    @PrePersist void onCreate() { var now=LocalDateTime.now(); createdAt=now; updatedAt=now; if(status==null) status=ApplicationStatus.UNKNOWN; }
    @PreUpdate void onUpdate() { updatedAt=LocalDateTime.now(); }
}
