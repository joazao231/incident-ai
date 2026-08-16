package dev.incidentai.backend.repository;

import dev.incidentai.backend.entity.MonitoredApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MonitoredApplicationRepository
        extends JpaRepository<MonitoredApplication, Long> {
    boolean existsByUrlIgnoreCase(String url);
    boolean existsByUrlIgnoreCaseAndIdNot(String url, Long id);
    List<MonitoredApplication> findByMonitoringEnabledTrue();
}
