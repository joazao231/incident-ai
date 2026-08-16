package dev.incidentai.backend.repository;
import dev.incidentai.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface IncidentRepository extends JpaRepository<Incident,Long>{List<Incident> findAllByOrderByOpenedAtDesc();List<Incident> findByStatusOrderByOpenedAtDesc(IncidentStatus status);Optional<Incident> findFirstByApplicationIdAndStatusInOrderByOpenedAtDesc(Long applicationId,Collection<IncidentStatus> statuses);void deleteByApplicationId(Long applicationId);}
