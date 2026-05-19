package com.aibert.dosw.infrastructure.messaging;

import com.aibert.dosw.application.dto.request.CreateNotificationRequest;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.CreateNotificationPort;
import com.aibert.dosw.infrastructure.messaging.event.SocialEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SocialEventConsumer")
class SocialEventConsumerTest {

    @Mock private CreateNotificationPort createNotificationPort;

    @InjectMocks
    private SocialEventConsumer consumer;

    @Test
    @DisplayName("valid social event → creates STUDY_SESSION_INVITE notification")
    void validEvent_createsStudySessionInvite() {
        SocialEvent event = SocialEvent.builder()
                .userId(1L).type("STUDY_SESSION_INVITE").title("Study group tonight")
                .message("Join us at 8pm").severity("LOW").relatedEntityId(20L)
                .build();

        consumer.consume(event);

        ArgumentCaptor<CreateNotificationRequest> captor =
                ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(createNotificationPort).create(captor.capture());

        CreateNotificationRequest req = captor.getValue();
        assertThat(req.getUserId()).isEqualTo(1L);
        assertThat(req.getType()).isEqualTo(NotificationType.STUDY_SESSION_INVITE);
        assertThat(req.getSeverity()).isEqualTo(NotificationSeverity.LOW);
        assertThat(req.getTitle()).isEqualTo("Study group tonight");
        assertThat(req.getRelatedEntityId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("INFO severity → creates notification with INFO severity")
    void infoSeverity_createsNotification() {
        SocialEvent event = SocialEvent.builder()
                .userId(2L).type("STUDY_SESSION_INVITE").title("Session")
                .message("msg").severity("INFO").relatedEntityId(null)
                .build();

        consumer.consume(event);

        ArgumentCaptor<CreateNotificationRequest> captor =
                ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(createNotificationPort).create(captor.capture());

        assertThat(captor.getValue().getSeverity()).isEqualTo(NotificationSeverity.INFO);
    }

    @Test
    @DisplayName("null severity → event discarded")
    void nullSeverity_eventDiscarded() {
        SocialEvent event = SocialEvent.builder()
                .userId(1L).type("STUDY_SESSION_INVITE").title("title").message("msg").severity(null)
                .build();

        consumer.consume(event);

        verify(createNotificationPort, never()).create(any());
    }

    @Test
    @DisplayName("unknown severity → event discarded")
    void unknownSeverity_eventDiscarded() {
        SocialEvent event = SocialEvent.builder()
                .userId(1L).type("STUDY_SESSION_INVITE").title("title").message("msg").severity("EXTREME")
                .build();

        consumer.consume(event);

        verify(createNotificationPort, never()).create(any());
    }
}
