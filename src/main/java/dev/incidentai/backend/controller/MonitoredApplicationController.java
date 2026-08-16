package dev.incidentai.backend.controller;
import dev.incidentai.backend.dto.*;import dev.incidentai.backend.service.*;import jakarta.validation.Valid;import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import java.util.List;
@RestController @RequestMapping("/api/applications")
public class MonitoredApplicationController {
 private final MonitoredApplicationService service;private final MonitoringService monitoring;
 public MonitoredApplicationController(MonitoredApplicationService service,MonitoringService monitoring){this.service=service;this.monitoring=monitoring;}
 @PostMapping ResponseEntity<ApplicationResponse> create(@Valid @RequestBody ApplicationRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));}
 @GetMapping List<ApplicationResponse> all(){return service.findAll();}@GetMapping("/{id}") ApplicationResponse one(@PathVariable Long id){return service.findById(id);}
 @PutMapping("/{id}") ApplicationResponse update(@PathVariable Long id,@Valid @RequestBody ApplicationRequest r){return service.update(id,r);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) void delete(@PathVariable Long id){service.delete(id);}
 @PostMapping("/{id}/check") ApplicationResponse check(@PathVariable Long id){return monitoring.check(id);}
 @GetMapping("/{id}/events") List<EventResponse> events(@PathVariable Long id){return monitoring.events(id);}
}
