package com.aibert.dosw.application.usecase.stats;

import com.aibert.dosw.application.dto.stats.LowGradeAlertDTO;
import com.aibert.dosw.application.dto.stats.OverloadAlertDTO;
import com.aibert.dosw.domain.model.notification.Notification;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.out.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    static final double GRADE_THRESHOLD = 3.0;

    private final NotificationRepositoryPort notificationRepository;

    public OverloadAlertDTO evaluateOverloadAlert(Long userId) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<Notification> alerts = notificationRepository
                .findByUserIdAndTypeAndCreatedAtAfter(userId, NotificationType.OVERLOAD_ALERT, since);

        Optional<Notification> latest = alerts.stream()
                .max(Comparator.comparing(Notification::getCreatedAt));

        if (latest.isEmpty()) {
            return OverloadAlertDTO.builder()
                    .active(false)
                    .title("No overload detected")
                    .message("Your workload is within your weekly availability.")
                    .build();
        }

        Notification n = latest.get();
        String bannerVariant = n.getSeverity() == NotificationSeverity.HIGH ? "critical" : "warning";

        return OverloadAlertDTO.builder()
                .active(true)
                .bannerVariant(bannerVariant)
                .title(n.getTitle())
                .message(n.getMessage())
                .suggestedAction("Consider rescheduling some tasks or reducing your academic load this week.")
                .build();
    }

    public LowGradeAlertDTO evaluateLowGradeAlert(Long userId) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<Notification> alerts = notificationRepository
                .findByUserIdAndTypeAndCreatedAtAfter(userId, NotificationType.LOW_PERFORMANCE_ALERT, since);

        Optional<Notification> latest = alerts.stream()
                .max(Comparator.comparing(Notification::getCreatedAt));

        if (latest.isEmpty()) {
            return LowGradeAlertDTO.builder()
                    .active(false)
                    .threshold(GRADE_THRESHOLD)
                    .subjectsAtRisk(Collections.emptyList())
                    .riskSubjects(Collections.emptyList())
                    .build();
        }

        Notification n = latest.get();
        String bannerVariant = n.getSeverity() == NotificationSeverity.HIGH ? "critical" : "warning";

        return LowGradeAlertDTO.builder()
                .active(true)
                .bannerVariant(bannerVariant)
                .title(n.getTitle())
                .alertTitle("Subjects at academic risk")
                .message(n.getMessage())
                .alertMessage(n.getMessage())
                .recommendation("Review each subject at risk and consult with your academic advisor.")
                .threshold(GRADE_THRESHOLD)
                .subjectsAtRisk(Collections.emptyList())
                .riskSubjects(Collections.emptyList())
                .generatedDate(n.getCreatedAt())
                .build();
    }
}
