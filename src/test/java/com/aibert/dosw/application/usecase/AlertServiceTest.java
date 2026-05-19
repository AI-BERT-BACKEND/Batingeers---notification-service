package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.stats.LowGradeAlertDTO;
import com.aibert.dosw.application.dto.stats.OverloadAlertDTO;
import com.aibert.dosw.application.usecase.stats.AlertService;
import com.aibert.dosw.domain.model.notification.Notification;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.out.NotificationRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertService")
class AlertServiceTest {

    @Mock private NotificationRepositoryPort notificationRepository;

    @InjectMocks
    private AlertService alertService;

    private final Long userId = 1L;

    // ─── evaluateOverloadAlert ───────────────────────────────────────────────

    @Test
    @DisplayName("Overload: no recent alert → inactive with default message")
    void overload_noRecentAlert_returnsInactive() {
        when(notificationRepository.findByUserIdAndTypeAndCreatedAtAfter(
                eq(userId), eq(NotificationType.OVERLOAD_ALERT), any()))
                .thenReturn(Collections.emptyList());

        OverloadAlertDTO result = alertService.evaluateOverloadAlert(userId);

        assertThat(result.isActive()).isFalse();
        assertThat(result.getTitle()).isEqualTo("No overload detected");
    }

    @Test
    @DisplayName("Overload: HIGH severity alert exists → active with critical banner")
    void overload_highSeverityAlert_returnsActiveCritical() {
        Notification alert = buildNotification(NotificationType.OVERLOAD_ALERT,
                "Critical overload", "You have 10 tasks (20.0 h) vs 8.0 h available.",
                NotificationSeverity.HIGH, LocalDateTime.now().minusHours(1));

        when(notificationRepository.findByUserIdAndTypeAndCreatedAtAfter(
                eq(userId), eq(NotificationType.OVERLOAD_ALERT), any()))
                .thenReturn(List.of(alert));

        OverloadAlertDTO result = alertService.evaluateOverloadAlert(userId);

        assertThat(result.isActive()).isTrue();
        assertThat(result.getBannerVariant()).isEqualTo("critical");
        assertThat(result.getTitle()).isEqualTo("Critical overload");
        assertThat(result.getMessage()).contains("10 tasks");
        assertThat(result.getSuggestedAction()).isNotBlank();
    }

    @Test
    @DisplayName("Overload: MEDIUM severity alert exists → active with warning banner")
    void overload_mediumSeverityAlert_returnsActiveWarning() {
        Notification alert = buildNotification(NotificationType.OVERLOAD_ALERT,
                "Possible overload", "You have 6 tasks (12.0 h) vs 10.0 h available.",
                NotificationSeverity.MEDIUM, LocalDateTime.now().minusHours(2));

        when(notificationRepository.findByUserIdAndTypeAndCreatedAtAfter(
                eq(userId), eq(NotificationType.OVERLOAD_ALERT), any()))
                .thenReturn(List.of(alert));

        OverloadAlertDTO result = alertService.evaluateOverloadAlert(userId);

        assertThat(result.isActive()).isTrue();
        assertThat(result.getBannerVariant()).isEqualTo("warning");
    }

    @Test
    @DisplayName("Overload: multiple alerts → uses the most recent one")
    void overload_multipleAlerts_usesMostRecent() {
        Notification older = buildNotification(NotificationType.OVERLOAD_ALERT,
                "Old overload", "Old message", NotificationSeverity.MEDIUM,
                LocalDateTime.now().minusHours(10));
        Notification newer = buildNotification(NotificationType.OVERLOAD_ALERT,
                "New critical overload", "New message", NotificationSeverity.HIGH,
                LocalDateTime.now().minusHours(1));

        when(notificationRepository.findByUserIdAndTypeAndCreatedAtAfter(
                eq(userId), eq(NotificationType.OVERLOAD_ALERT), any()))
                .thenReturn(List.of(older, newer));

        OverloadAlertDTO result = alertService.evaluateOverloadAlert(userId);

        assertThat(result.getTitle()).isEqualTo("New critical overload");
        assertThat(result.getBannerVariant()).isEqualTo("critical");
    }

    // ─── evaluateLowGradeAlert ───────────────────────────────────────────────

    @Test
    @DisplayName("LowGrade: no recent alert → inactive with threshold 3.0")
    void lowGrade_noRecentAlert_returnsInactive() {
        when(notificationRepository.findByUserIdAndTypeAndCreatedAtAfter(
                eq(userId), eq(NotificationType.LOW_PERFORMANCE_ALERT), any()))
                .thenReturn(Collections.emptyList());

        LowGradeAlertDTO result = alertService.evaluateLowGradeAlert(userId);

        assertThat(result.isActive()).isFalse();
        assertThat(result.getThreshold()).isEqualTo(3.0);
        assertThat(result.getSubjectsAtRisk()).isEmpty();
    }

    @Test
    @DisplayName("LowGrade: HIGH severity alert exists → active with critical banner and alertTitle")
    void lowGrade_highSeverityAlert_returnsActiveCritical() {
        Notification alert = buildNotification(NotificationType.LOW_PERFORMANCE_ALERT,
                "Low academic performance", "Your average is 2.4. Subjects at risk: Calculus I.",
                NotificationSeverity.HIGH, LocalDateTime.now().minusHours(1));

        when(notificationRepository.findByUserIdAndTypeAndCreatedAtAfter(
                eq(userId), eq(NotificationType.LOW_PERFORMANCE_ALERT), any()))
                .thenReturn(List.of(alert));

        LowGradeAlertDTO result = alertService.evaluateLowGradeAlert(userId);

        assertThat(result.isActive()).isTrue();
        assertThat(result.getBannerVariant()).isEqualTo("critical");
        assertThat(result.getAlertTitle()).isEqualTo("Subjects at academic risk");
        assertThat(result.getRecommendation()).isNotBlank();
        assertThat(result.getGeneratedDate()).isNotNull();
    }

    @Test
    @DisplayName("LowGrade: MEDIUM severity alert exists → active with warning banner")
    void lowGrade_mediumSeverityAlert_returnsActiveWarning() {
        Notification alert = buildNotification(NotificationType.LOW_PERFORMANCE_ALERT,
                "Low academic performance", "Your average is 2.8.",
                NotificationSeverity.MEDIUM, LocalDateTime.now().minusHours(2));

        when(notificationRepository.findByUserIdAndTypeAndCreatedAtAfter(
                eq(userId), eq(NotificationType.LOW_PERFORMANCE_ALERT), any()))
                .thenReturn(List.of(alert));

        LowGradeAlertDTO result = alertService.evaluateLowGradeAlert(userId);

        assertThat(result.isActive()).isTrue();
        assertThat(result.getBannerVariant()).isEqualTo("warning");
        assertThat(result.getMessage()).contains("2.8");
    }

    @Test
    @DisplayName("LowGrade: multiple alerts → uses the most recent one")
    void lowGrade_multipleAlerts_usesMostRecent() {
        Notification older = buildNotification(NotificationType.LOW_PERFORMANCE_ALERT,
                "Old alert", "Old message", NotificationSeverity.MEDIUM,
                LocalDateTime.now().minusHours(20));
        Notification newer = buildNotification(NotificationType.LOW_PERFORMANCE_ALERT,
                "New alert", "New message", NotificationSeverity.HIGH,
                LocalDateTime.now().minusHours(1));

        when(notificationRepository.findByUserIdAndTypeAndCreatedAtAfter(
                eq(userId), eq(NotificationType.LOW_PERFORMANCE_ALERT), any()))
                .thenReturn(List.of(older, newer));

        LowGradeAlertDTO result = alertService.evaluateLowGradeAlert(userId);

        assertThat(result.getTitle()).isEqualTo("New alert");
    }

    private Notification buildNotification(NotificationType type, String title, String message,
                                           NotificationSeverity severity, LocalDateTime createdAt) {
        return Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .severity(severity)
                .createdAt(createdAt)
                .build();
    }
}
