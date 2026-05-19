package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.response.NotificationResponse;
import com.aibert.dosw.application.mapper.NotificationMapper;
import com.aibert.dosw.application.usecase.notification.GetAlertsUseCase;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAlertsUseCase")
class GetAlertsUseCaseTest {

    @Mock private NotificationRepositoryPort repository;
    @Mock private NotificationMapper mapper;

    @InjectMocks
    private GetAlertsUseCase useCase;

    private final Long userId = 1L;

    @Test
    @DisplayName("returns stored alert notifications from the last 7 days")
    void returnsStoredAlerts() {
        Notification alert = Notification.builder()
                .id(1L).userId(userId)
                .type(NotificationType.OVERLOAD_ALERT)
                .severity(NotificationSeverity.HIGH)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        NotificationResponse response = NotificationResponse.builder()
                .id(1L).type(NotificationType.OVERLOAD_ALERT).build();

        when(repository.findByUserIdAndTypeInAndCreatedAtAfter(eq(userId), anyList(), any()))
                .thenReturn(List.of(alert));
        when(mapper.toResponseList(List.of(alert))).thenReturn(List.of(response));

        List<NotificationResponse> result = useCase.getActiveAlerts(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(NotificationType.OVERLOAD_ALERT);
    }

    @Test
    @DisplayName("returns empty list when no alerts exist in the last 7 days")
    void returnsEmptyWhenNoAlerts() {
        when(repository.findByUserIdAndTypeInAndCreatedAtAfter(eq(userId), anyList(), any()))
                .thenReturn(Collections.emptyList());
        when(mapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<NotificationResponse> result = useCase.getActiveAlerts(userId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("returns both OVERLOAD_ALERT and LOW_PERFORMANCE_ALERT types")
    void returnsBothAlertTypes() {
        Notification overload = Notification.builder()
                .id(1L).userId(userId).type(NotificationType.OVERLOAD_ALERT)
                .severity(NotificationSeverity.HIGH).createdAt(LocalDateTime.now().minusHours(2)).build();
        Notification lowGrade = Notification.builder()
                .id(2L).userId(userId).type(NotificationType.LOW_PERFORMANCE_ALERT)
                .severity(NotificationSeverity.MEDIUM).createdAt(LocalDateTime.now().minusHours(1)).build();

        when(repository.findByUserIdAndTypeInAndCreatedAtAfter(eq(userId), anyList(), any()))
                .thenReturn(List.of(overload, lowGrade));
        when(mapper.toResponseList(any())).thenReturn(List.of(
                NotificationResponse.builder().id(1L).type(NotificationType.OVERLOAD_ALERT).build(),
                NotificationResponse.builder().id(2L).type(NotificationType.LOW_PERFORMANCE_ALERT).build()));

        List<NotificationResponse> result = useCase.getActiveAlerts(userId);

        assertThat(result).hasSize(2);
    }
}
