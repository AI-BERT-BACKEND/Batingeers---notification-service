package com.aibert.dosw.infrastructure.messaging;

import com.aibert.dosw.application.dto.request.CreateNotificationRequest;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.CreateNotificationPort;
import com.aibert.dosw.infrastructure.messaging.event.GamificationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GamificationEventConsumer")
class GamificationEventConsumerTest {

    @Mock private CreateNotificationPort createNotificationPort;

    @InjectMocks
    private GamificationEventConsumer consumer;

    @Test
    @DisplayName("valid level-up event → creates LEVEL_UP notification with INFO severity")
    void validEvent_createsLevelUpNotification() {
        GamificationEvent event = GamificationEvent.builder()
                .userId(1L)
                .newLevelNumber(5)
                .previousLevelNumber(4)
                .levelName("Scholar")
                .build();

        consumer.consume(event);

        ArgumentCaptor<CreateNotificationRequest> captor =
                ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(createNotificationPort).create(captor.capture());

        CreateNotificationRequest request = captor.getValue();
        assertThat(request.getUserId()).isEqualTo(1L);
        assertThat(request.getType()).isEqualTo(NotificationType.LEVEL_UP);
        assertThat(request.getSeverity()).isEqualTo(NotificationSeverity.INFO);
        assertThat(request.getTitle()).contains("5").contains("Scholar");
        assertThat(request.getMessage()).contains("4").contains("5");
    }

    @Test
    @DisplayName("event without levelName → title still contains level number")
    void eventWithoutLevelName_titleContainsLevelNumber() {
        GamificationEvent event = GamificationEvent.builder()
                .userId(2L)
                .newLevelNumber(3)
                .previousLevelNumber(2)
                .levelName(null)
                .build();

        consumer.consume(event);

        ArgumentCaptor<CreateNotificationRequest> captor =
                ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(createNotificationPort).create(captor.capture());

        assertThat(captor.getValue().getTitle()).contains("3");
        assertThat(captor.getValue().getMessage()).contains("2").contains("3");
    }

    @Test
    @DisplayName("event with null userId → discarded, no notification created")
    void nullUserId_discardedWithoutCreatingNotification() {
        GamificationEvent event = GamificationEvent.builder()
                .userId(null)
                .newLevelNumber(5)
                .previousLevelNumber(4)
                .levelName("Scholar")
                .build();

        consumer.consume(event);

        verify(createNotificationPort, never()).create(org.mockito.ArgumentMatchers.any());
    }
}
