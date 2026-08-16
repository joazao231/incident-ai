package dev.incidentai.backend.service;

import dev.incidentai.backend.dto.*;
import dev.incidentai.backend.entity.*;
import dev.incidentai.backend.exception.ResourceNotFoundException;
import dev.incidentai.backend.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.*;

@Service
public class MonitoringService {
 private final MonitoredApplicationRepository apps;private final MonitoringEventRepository events;private final IncidentRepository incidents;private final HttpClient client;
 @Value("${incident-ai.monitoring.timeout-ms:5000}") long timeoutMs;@Value("${incident-ai.monitoring.degraded-threshold-ms:1500}") long degradedMs;
 public MonitoringService(MonitoredApplicationRepository apps,MonitoringEventRepository events,IncidentRepository incidents){this.apps=apps;this.events=events;this.incidents=incidents;this.client=HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();}
 @Scheduled(fixedDelayString="${incident-ai.monitoring.fixed-delay-ms:60000}",initialDelayString="${incident-ai.monitoring.fixed-delay-ms:60000}")
 public void scheduledCheck(){apps.findByMonitoringEnabledTrue().forEach(a->{try{check(a.getId());}catch(RuntimeException ignored){}});}
 @Transactional public ApplicationResponse check(Long id){var a=apps.findById(id).orElseThrow(()->new ResourceNotFoundException("Aplicação "+id+" não encontrada"));if(!a.isMonitoringEnabled())throw new IllegalStateException("Monitoramento desabilitado para esta aplicação");var start=System.nanoTime();Integer code=null;String error=null;ApplicationStatus status;try{var request=HttpRequest.newBuilder(URI.create(a.getUrl())).timeout(Duration.ofMillis(timeoutMs)).GET().build();var response=client.send(request,HttpResponse.BodyHandlers.discarding());code=response.statusCode();long elapsed=elapsed(start);status=code>=200&&code<400?(elapsed>=degradedMs?ApplicationStatus.DEGRADED:ApplicationStatus.HEALTHY):ApplicationStatus.DOWN;a.setLastResponseTimeMs(elapsed);}catch(Exception e){status=ApplicationStatus.DOWN;error=safe(e.getMessage());a.setLastResponseTimeMs(elapsed(start));}
  var previous=a.getStatus();a.setStatus(status);a.setLastStatusCode(code);a.setLastError(error);a.setLastCheckedAt(LocalDateTime.now());apps.save(a);var type=status==ApplicationStatus.HEALTHY?EventType.CHECK_SUCCEEDED:status==ApplicationStatus.DEGRADED?EventType.CHECK_DEGRADED:EventType.CHECK_FAILED;record(a,type,error==null?"Verificação concluída com status "+status:error,code,a.getLastResponseTimeMs());manageIncident(a,previous);return ApplicationResponse.from(a);}
 @Transactional(readOnly=true) public List<EventResponse> events(Long appId){if(!apps.existsById(appId))throw new ResourceNotFoundException("Aplicação "+appId+" não encontrada");return events.findTop100ByApplicationIdOrderByOccurredAtDesc(appId).stream().map(EventResponse::from).toList();}
 private void manageIncident(MonitoredApplication a,ApplicationStatus previous){var active=incidents.findFirstByApplicationIdAndStatusInOrderByOpenedAtDesc(a.getId(),List.of(IncidentStatus.OPEN,IncidentStatus.ACKNOWLEDGED));if(a.getStatus()==ApplicationStatus.DOWN&&active.isEmpty()){var i=incidents.save(Incident.builder().application(a).title(a.getName()+" indisponível").description(a.getLastError()==null?"Endpoint respondeu HTTP "+a.getLastStatusCode():a.getLastError()).severity(a.getEnvironment()==Environment.PRODUCTION?IncidentSeverity.CRITICAL:IncidentSeverity.HIGH).status(IncidentStatus.OPEN).build());record(a,EventType.INCIDENT_OPENED,"Incidente "+i.getId()+" aberto automaticamente",a.getLastStatusCode(),a.getLastResponseTimeMs());}else if(a.getStatus()==ApplicationStatus.HEALTHY&&active.isPresent()){var i=active.get();i.setStatus(IncidentStatus.RESOLVED);i.setResolvedAt(LocalDateTime.now());incidents.save(i);record(a,EventType.INCIDENT_RESOLVED,"Incidente "+i.getId()+" resolvido automaticamente",a.getLastStatusCode(),a.getLastResponseTimeMs());}}
 private void record(MonitoredApplication a,EventType type,String message,Integer code,Long ms){events.save(MonitoringEvent.builder().application(a).type(type).message(safe(message)).statusCode(code).responseTimeMs(ms).build());}
 private long elapsed(long start){return (System.nanoTime()-start)/1_000_000;}private String safe(String value){if(value==null||value.isBlank())return "Falha sem detalhes";return value.length()>500?value.substring(0,500):value;}
}
