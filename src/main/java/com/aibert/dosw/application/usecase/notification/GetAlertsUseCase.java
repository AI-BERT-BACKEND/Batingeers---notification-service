package com.aibert.dosw.application.usecase.notification;

import com.aibert.dosw.application.dto.external.AcademicPerformanceData;
import com.aibert.dosw.application.dto.external.TaskWorkloadData;
import com.aibert.dosw.application.dto.response.NotificationResponse;
import com.aibert.dosw.application.mapper.NotificationMapper;
import com.aibert.dosw.domain.model.notification.Notification;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.GetAlertsPort;
import com.aibert.dosw.domain.ports.out.AcademicServicePort;
import com.aibert.dosw.domain.ports.out.NotificationRepositoryPort;
import com.aibert.dosw.domain.ports.out.TaskServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetAlertsUseCase implements GetAlertsPort {

    private final NotificationRepositoryPort repository;
    private final TaskServicePort taskService;
    private final AcademicServicePort academicService;
    private final NotificationMapper mapper;

    private static final List<NotificationType> ALERT_TYPES = List.of(
            NotificationType.OVERLOAD_ALERT,
            NotificationType.LOW_PERFORMANCE_ALERT
    );

    @Override
    @Transactional
    public List<NotificationResponse> getActiveAlerts(Long userId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Notification> alerts = new ArrayList<>(
                repository.findByUserIdAndTypeInAndCreatedAtAfter(userId, ALERT_TYPES, sevenDaysAgo));

        checkAndGenerateOverloadAlert(userId, alerts);
        checkAndGenerateLowPerformanceAlert(userId, alerts);

        return mapper.toResponseList(alerts);
    }

    private void checkAndGenerateOverloadAlert(Long userId, List<Notification> alerts) {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIDNIGHT);
        boolean hasOverloadToday = alerts.stream()
                .anyMatch(n -> n.getType() == NotificationType.OVERLOAD_ALERT
                        && n.getCreatedAt().isAfter(startOfDay));

        if (hasOverloadToday) return;

        try {
            taskService.getUserWorkload(userId).ifPresent(workload -> {
                if (workload.isHigh()) {
                    Notification alert = buildOverloadAlert(userId, workload);
                    alerts.add(repository.save(alert));
                }
            });
        } catch (Exception ex) {
            log.warn("No se pudo consultar la carga de tareas para userId={}: {}", userId, ex.getMessage());
        }
    }

    private void checkAndGenerateLowPerformanceAlert(Long userId, List<Notification> alerts) {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIDNIGHT);
        boolean hasPerformanceAlertToday = alerts.stream()
                .anyMatch(n -> n.getType() == NotificationType.LOW_PERFORMANCE_ALERT
                        && n.getCreatedAt().isAfter(startOfDay));

        if (hasPerformanceAlertToday) return;

        try {
            academicService.getUserPerformance(userId).ifPresent(performance -> {
                if (performance.isAtRisk()) {
                    Notification alert = buildLowPerformanceAlert(userId, performance);
                    alerts.add(repository.save(alert));
                }
            });
        } catch (Exception ex) {
            log.warn("No se pudo consultar el rendimiento para userId={}: {}", userId, ex.getMessage());
        }
    }

    private Notification buildOverloadAlert(Long userId, TaskWorkloadData workload) {
        String message = String.format(
                "Tienes %d tareas activas (%d urgentes, %d vencidas). Considera redistribuir tu carga.",
                workload.getTotalTasks(), workload.getUrgentTasks(), workload.getOverdueTasks());

        NotificationSeverity severity = workload.isCritical()
                ? NotificationSeverity.HIGH : NotificationSeverity.MEDIUM;

        return Notification.builder()
                .userId(userId)
                .type(NotificationType.OVERLOAD_ALERT)
                .title("Alerta de sobrecarga académica")
                .message(message)
                .severity(severity)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Notification buildLowPerformanceAlert(Long userId, AcademicPerformanceData performance) {
        String subjects = String.join(", ", performance.getAtRiskSubjectNames());
        String message = String.format(
                "Tu promedio general es %.1f. Las materias en riesgo son: %s.",
                performance.getOverallAverage(), subjects);

        return Notification.builder()
                .userId(userId)
                .type(NotificationType.LOW_PERFORMANCE_ALERT)
                .title("Alerta de bajo rendimiento")
                .message(message)
                .severity(NotificationSeverity.HIGH)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
