package com.aibert.dosw.infrastructure.messaging;

import com.aibert.dosw.application.dto.request.CreateNotificationRequest;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.CreateNotificationPort;
import com.aibert.dosw.infrastructure.messaging.event.GamificationEvent;
import com.aibert.dosw.infrastructure.messaging.event.LevelName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GamificationEventConsumer")
class GamificationEventConsumerTest {

    @Mock private CreateNotificationPort createNotificationPort;

    @InjectMocks
    private GamificationEventConsumer consumer;

    private static final UUID USER_ID_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    @DisplayName("valid level-up event → creates LEVEL_UP notification with INFO severity")
    void validEvent_createsLevelUpNotification() {
        GamificationEvent event = GamificationEvent.builder()
                .userId(USER_ID_1)
                .newLevelNumber(5)
                .previousLevelNumber(4)
                .levelName(LevelName.AVANZADO)
                .build();

        consumer.consume(event);

        ArgumentCaptor<CreateNotificationRequest> captor =
                ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(createNotificationPort).create(captor.capture());

        CreateNotificationRequest request = captor.getValue();
        assertThat(request.getUserId()).isEqualTo(USER_ID_1);
        assertThat(request.getType()).isEqualTo(NotificationType.LEVEL_UP);
        assertThat(request.getSeverity()).isEqualTo(NotificationSeverity.INFO);
        assertThat(request.getTitle()).contains("5").contains("AVANZADO");
        assertThat(request.getMessage()).contains("4").contains("5");
    }

    @Test
    @DisplayName("event without levelName → title still contains level number")
    void eventWithoutLevelName_titleContainsLevelNumber() {
        GamificationEvent event = GamificationEvent.builder()
                .userId(USER_ID_2)
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
                .levelName(LevelName.AVANZADO)
                .build();

        consumer.consume(event);

        verify(createNotificationPort, never()).create(org.mockito.ArgumentMatchers.any());
    }
}
