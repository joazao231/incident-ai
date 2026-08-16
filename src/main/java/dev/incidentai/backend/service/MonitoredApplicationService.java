package dev.incidentai.backend.service;
import dev.incidentai.backend.dto.*;import dev.incidentai.backend.entity.*;import dev.incidentai.backend.exception.*;import dev.incidentai.backend.repository.*;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import java.util.List;
@Service @Transactional
public class MonitoredApplicationService {
 private final MonitoredApplicationRepository apps;private final MonitoringEventRepository events;private final IncidentRepository incidents;
 public MonitoredApplicationService(MonitoredApplicationRepository apps,MonitoringEventRepository events,IncidentRepository incidents){this.apps=apps;this.events=events;this.incidents=incidents;}
 public ApplicationResponse create(ApplicationRequest r){if(apps.existsByUrlIgnoreCase(r.url()))throw new BusinessException("Já existe uma aplicação com esta URL");var a=MonitoredApplication.builder().name(r.name().trim()).url(r.url().trim()).environment(r.environment()).monitoringEnabled(r.monitoringEnabled()==null||r.monitoringEnabled()).status(ApplicationStatus.UNKNOWN).build();return ApplicationResponse.from(apps.save(a));}
 @Transactional(readOnly=true) public List<ApplicationResponse> findAll(){return apps.findAll().stream().map(ApplicationResponse::from).toList();}
 @Transactional(readOnly=true) public ApplicationResponse findById(Long id){return ApplicationResponse.from(get(id));}
 public ApplicationResponse update(Long id,ApplicationRequest r){var a=get(id);if(apps.existsByUrlIgnoreCaseAndIdNot(r.url(),id))throw new BusinessException("Já existe uma aplicação com esta URL");a.setName(r.name().trim());a.setUrl(r.url().trim());a.setEnvironment(r.environment());if(r.monitoringEnabled()!=null)a.setMonitoringEnabled(r.monitoringEnabled());return ApplicationResponse.from(apps.save(a));}
 public void delete(Long id){var a=get(id);events.deleteByApplicationId(id);incidents.deleteByApplicationId(id);apps.delete(a);}
 MonitoredApplication get(Long id){return apps.findById(id).orElseThrow(()->new ResourceNotFoundException("Aplicação "+id+" não encontrada"));}
}
