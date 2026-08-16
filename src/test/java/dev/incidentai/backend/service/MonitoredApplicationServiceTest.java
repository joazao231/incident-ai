package dev.incidentai.backend.service;
import dev.incidentai.backend.dto.*;import dev.incidentai.backend.entity.*;import dev.incidentai.backend.exception.BusinessException;import dev.incidentai.backend.repository.*;import org.junit.jupiter.api.*;import org.junit.jupiter.api.extension.ExtendWith;import org.mockito.*;import org.mockito.junit.jupiter.MockitoExtension;import java.util.Optional;import static org.assertj.core.api.Assertions.*;import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class MonitoredApplicationServiceTest {
 @Mock MonitoredApplicationRepository apps;@Mock MonitoringEventRepository events;@Mock IncidentRepository incidents;@InjectMocks MonitoredApplicationService service;
 @Test void createsApplicationWithDefaults(){var request=new ApplicationRequest("API","https://example.com",Environment.PRODUCTION,null);when(apps.save(any())).thenAnswer(i->{var a=(MonitoredApplication)i.getArgument(0);a.setId(1L);return a;});var result=service.create(request);assertThat(result.id()).isEqualTo(1L);assertThat(result.monitoringEnabled()).isTrue();assertThat(result.status()).isEqualTo(ApplicationStatus.UNKNOWN);}
 @Test void rejectsDuplicateUrl(){when(apps.existsByUrlIgnoreCase("https://example.com")).thenReturn(true);var request=new ApplicationRequest("API","https://example.com",Environment.PRODUCTION,true);assertThatThrownBy(()->service.create(request)).isInstanceOf(BusinessException.class);}
 @Test void deletesRelatedDataBeforeApplication(){var app=MonitoredApplication.builder().id(9L).build();when(apps.findById(9L)).thenReturn(Optional.of(app));service.delete(9L);var order=inOrder(events,incidents,apps);order.verify(events).deleteByApplicationId(9L);order.verify(incidents).deleteByApplicationId(9L);order.verify(apps).delete(app);}
}
