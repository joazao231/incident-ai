package dev.incidentai.backend.repository;
import dev.incidentai.backend.entity.MonitoringEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MonitoringEventRepository extends JpaRepository<MonitoringEvent,Long>{List<MonitoringEvent> findTop100ByApplicationIdOrderByOccurredAtDesc(Long applicationId);void deleteByApplicationId(Long applicationId);}
