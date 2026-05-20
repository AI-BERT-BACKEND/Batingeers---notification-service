package com.aibert.dosw.infrastructure.messaging;

import com.aibert.dosw.application.dto.request.CreateNotificationRequest;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.CreateNotificationPort;
import com.aibert.dosw.infrastructure.messaging.event.SocialEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

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

    private static final UUID USER_ID_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID RELATED_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");

    @Test
    @DisplayName("STUDY_SESSION_INVITE event → creates notification")
    void studySessionInvite_createsNotification() {
        SocialEvent event = SocialEvent.builder()
                .userId(USER_ID_1).type("STUDY_SESSION_INVITE").title("Study group tonight")
                .message("Join us at 8pm").severity("LOW").relatedEntityId(RELATED_ID)
                .build();

        consumer.consume(event);

        ArgumentCaptor<CreateNotificationRequest> captor =
                ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(createNotificationPort).create(captor.capture());

        CreateNotificationRequest req = captor.getValue();
        assertThat(req.getUserId()).isEqualTo(USER_ID_1);
        assertThat(req.getType()).isEqualTo(NotificationType.STUDY_SESSION_INVITE);
        assertThat(req.getSeverity()).isEqualTo(NotificationSeverity.LOW);
        assertThat(req.getTitle()).isEqualTo("Study group tonight");
        assertThat(req.getRelatedEntityId()).isEqualTo(RELATED_ID);
    }

    @Test
    @DisplayName("CONNECTION_REQUEST_RECEIVED event → creates notification")
    void connectionRequestReceived_createsNotification() {
        SocialEvent event = SocialEvent.builder()
                .userId(USER_ID_1).type("CONNECTION_REQUEST_RECEIVED").title("Nueva solicitud")
                .message("Alguien quiere conectar contigo").severity("INFO").relatedEntityId(RELATED_ID)
                .build();

        consumer.consume(event);

        ArgumentCaptor<CreateNotificationRequest> captor =
                ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(createNotificationPort).create(captor.capture());

        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.CONNECTION_REQUEST_RECEIVED);
    }

    @Test
    @DisplayName("CONNECTION_REQUEST_ACCEPTED event → creates notification")
    void connectionRequestAccepted_createsNotification() {
        SocialEvent event = SocialEvent.builder()
                .userId(USER_ID_2).type("CONNECTION_REQUEST_ACCEPTED").title("Solicitud aceptada")
                .message("Tu solicitud fue aceptada").severity("INFO").relatedEntityId(RELATED_ID)
                .build();

        consumer.consume(event);

        ArgumentCaptor<CreateNotificationRequest> captor =
                ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(createNotificationPort).create(captor.capture());

        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.CONNECTION_REQUEST_ACCEPTED);
    }

    @Test
    @DisplayName("INFO severity → creates notification with INFO severity")
    void infoSeverity_createsNotification() {
        SocialEvent event = SocialEvent.builder()
                .userId(USER_ID_2).type("STUDY_SESSION_INVITE").title("Session")
                .message("msg").severity("INFO").relatedEntityId(null)
                .build();

        consumer.consume(event);

        ArgumentCaptor<CreateNotificationRequest> captor =
                ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(createNotificationPort).create(captor.capture());

        assertThat(captor.getValue().getSeverity()).isEqualTo(NotificationSeverity.INFO);
    }

    static Stream<Arguments> invalidEvents() {
        return Stream.of(
                Arguments.of("STUDY_SESSION_INVITE", null,      "null severity"),
                Arguments.of("STUDY_SESSION_INVITE", "EXTREME", "unknown severity"),
                Arguments.of("OVERLOAD_ALERT",       "INFO",    "unknown type for social.events")
        );
    }

    @ParameterizedTest(name = "{2} → event discarded")
    @MethodSource("invalidEvents")
    @DisplayName("invalid events → no notification created")
    void invalidEvent_eventDiscarded(String type, String severity, String scenario) {
        SocialEvent event = SocialEvent.builder()
                .userId(USER_ID_1).type(type).title("title").message("msg").severity(severity)
                .build();

        consumer.consume(event);

        verify(createNotificationPort, never()).create(any());
    }
}
