package dev.incidentai.backend.service;
import dev.incidentai.backend.dto.*;import dev.incidentai.backend.entity.*;import dev.incidentai.backend.exception.*;import dev.incidentai.backend.repository.*;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import java.time.LocalDateTime;import java.util.*;
@Service @Transactional
public class IncidentService {
 private final IncidentRepository incidents;private final MonitoredApplicationRepository apps;
 public IncidentService(IncidentRepository incidents,MonitoredApplicationRepository apps){this.incidents=incidents;this.apps=apps;}
 public IncidentResponse create(IncidentRequest r){var a=apps.findById(r.applicationId()).orElseThrow(()->new ResourceNotFoundException("Aplicação não encontrada"));return IncidentResponse.from(incidents.save(Incident.builder().application(a).title(r.title()).description(r.description()).severity(r.severity()).status(IncidentStatus.OPEN).build()));}
 @Transactional(readOnly=true) public List<IncidentResponse> findAll(IncidentStatus status){var list=status==null?incidents.findAllByOrderByOpenedAtDesc():incidents.findByStatusOrderByOpenedAtDesc(status);return list.stream().map(IncidentResponse::from).toList();}
 @Transactional(readOnly=true) public IncidentResponse find(Long id){return IncidentResponse.from(get(id));}
 public IncidentResponse acknowledge(Long id){var i=get(id);if(i.getStatus()==IncidentStatus.RESOLVED)throw new BusinessException("Incidente resolvido não pode ser reconhecido");i.setStatus(IncidentStatus.ACKNOWLEDGED);i.setAcknowledgedAt(LocalDateTime.now());return IncidentResponse.from(i);}
 public IncidentResponse resolve(Long id){var i=get(id);i.setStatus(IncidentStatus.RESOLVED);i.setResolvedAt(LocalDateTime.now());return IncidentResponse.from(i);}
 private Incident get(Long id){return incidents.findById(id).orElseThrow(()->new ResourceNotFoundException("Incidente "+id+" não encontrado"));}
}
