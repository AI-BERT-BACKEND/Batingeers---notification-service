package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.external.AcademicPerformanceData;
import com.aibert.dosw.application.dto.external.TaskWorkloadData;
import com.aibert.dosw.application.dto.response.NotificationResponse;
import com.aibert.dosw.application.mapper.NotificationMapper;
import com.aibert.dosw.application.usecase.notification.GetAlertsUseCase;
import com.aibert.dosw.domain.model.notification.Notification;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.out.AcademicServicePort;
import com.aibert.dosw.domain.ports.out.NotificationRepositoryPort;
import com.aibert.dosw.domain.ports.out.TaskServicePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAlertsUseCase")
class GetAlertsUseCaseTest {

    @Mock
    private NotificationRepositoryPort repository;

    @Mock
    private TaskServicePort taskService;

    @Mock
    private AcademicServicePort academicService;

    @Mock
    private NotificationMapper mapper;

    @InjectMocks
    private GetAlertsUseCase useCase;

    private final Long userId = 1L;

    @Test
    @DisplayName("debe generar alerta de sobrecarga cuando la carga es CRITICAL")
    void shouldGenerateOverloadAlertWhenCriticalWorkload() {
        TaskWorkloadData workload = TaskWorkloadData.builder()
                .userId(userId)
                .totalTasks(12)
                .urgentTasks(7)
                .overdueTasks(3)
                .workloadLevel("CRITICAL")
                .build();

        Notification savedAlert = Notification.builder()
                .id(1L)
                .userId(userId)
                .type(NotificationType.OVERLOAD_ALERT)
                .title("Alerta de sobrecarga académica")
                .severity(NotificationSeverity.HIGH)
                .createdAt(LocalDateTime.now())
                .build();

        NotificationResponse response = NotificationResponse.builder()
                .id(1L).type(NotificationType.OVERLOAD_ALERT).build();

        when(repository.findByUserIdAndTypeInAndCreatedAtAfter(eq(userId), anyList(), any()))
                .thenReturn(Collections.emptyList());
        when(taskService.getUserWorkload(userId)).thenReturn(Optional.of(workload));
        when(academicService.getUserPerformance(userId)).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(savedAlert);
        when(mapper.toResponseList(any())).thenReturn(List.of(response));

        List<NotificationResponse> result = useCase.getActiveAlerts(userId);

        assertThat(result).hasSize(1);
        verify(repository).save(any());
    }

    @Test
    @DisplayName("debe generar alerta de bajo rendimiento cuando el usuario está en riesgo")
    void shouldGenerateLowPerformanceAlertWhenAtRisk() {
        AcademicPerformanceData performance = AcademicPerformanceData.builder()
                .userId(userId)
                .atRiskSubjectNames(List.of("Cálculo I", "Física II"))
                .overallAverage(2.8)
                .atRisk(true)
                .build();

        Notification savedAlert = Notification.builder()
                .id(2L)
                .userId(userId)
                .type(NotificationType.LOW_PERFORMANCE_ALERT)
                .title("Alerta de bajo rendimiento")
                .severity(NotificationSeverity.HIGH)
                .createdAt(LocalDateTime.now())
                .build();

        NotificationResponse response = NotificationResponse.builder()
                .id(2L).type(NotificationType.LOW_PERFORMANCE_ALERT).build();

        when(repository.findByUserIdAndTypeInAndCreatedAtAfter(eq(userId), anyList(), any()))
                .thenReturn(Collections.emptyList());
        when(taskService.getUserWorkload(userId)).thenReturn(Optional.empty());
        when(academicService.getUserPerformance(userId)).thenReturn(Optional.of(performance));
        when(repository.save(any())).thenReturn(savedAlert);
        when(mapper.toResponseList(any())).thenReturn(List.of(response));

        List<NotificationResponse> result = useCase.getActiveAlerts(userId);

        assertThat(result).hasSize(1);
        verify(repository).save(any());
    }

    @Test
    @DisplayName("no debe generar alertas si ya existen para hoy")
    void shouldNotDuplicateAlertsWhenAlreadyExistToday() {
        Notification existingAlert = Notification.builder()
                .id(1L)
                .userId(userId)
                .type(NotificationType.OVERLOAD_ALERT)
                .severity(NotificationSeverity.HIGH)
                .createdAt(LocalDateTime.now())
                .build();

        NotificationResponse response = NotificationResponse.builder()
                .id(1L).type(NotificationType.OVERLOAD_ALERT).build();

        when(repository.findByUserIdAndTypeInAndCreatedAtAfter(eq(userId), anyList(), any()))
                .thenReturn(List.of(existingAlert));
        when(academicService.getUserPerformance(userId)).thenReturn(Optional.empty());
        when(mapper.toResponseList(any())).thenReturn(List.of(response));

        List<NotificationResponse> result = useCase.getActiveAlerts(userId);

        assertThat(result).hasSize(1);
    }
}
