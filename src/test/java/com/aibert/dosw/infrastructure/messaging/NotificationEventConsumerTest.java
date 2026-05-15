package com.aibert.dosw.infrastructure.messaging;

import com.aibert.dosw.application.dto.request.CreateNotificationRequest;
import com.aibert.dosw.application.dto.response.NotificationResponse;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.CreateNotificationPort;
import com.aibert.dosw.infrastructure.messaging.event.NotificationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventConsumer")
class NotificationEventConsumerTest {

    @Mock
    private CreateNotificationPort createNotificationPort;

    @InjectMocks
    private NotificationEventConsumer consumer;

    @Test
    @DisplayName("consume: evento válido → crea notificación con campos correctos")
    void consume_validEvent_createsNotification() {
        NotificationEvent event = NotificationEvent.builder()
                .userId(1L)
                .type("TASK_REMINDER")
                .title("Tarea próxima a vencer")
                .message("Tu tarea 'Parcial Cálculo' vence mañana.")
                .severity("HIGH")
                .relatedEntityId(42L)
                .build();

        when(createNotificationPort.create(any())).thenReturn(mock(NotificationResponse.class));

        consumer.consume(event);

        ArgumentCaptor<CreateNotificationRequest> captor =
                ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(createNotificationPort).create(captor.capture());

        CreateNotificationRequest request = captor.getValue();
        assertThat(request.getUserId()).isEqualTo(1L);
        assertThat(request.getType()).isEqualTo(NotificationType.TASK_REMINDER);
        assertThat(request.getTitle()).isEqualTo("Tarea próxima a vencer");
        assertThat(request.getSeverity()).isEqualTo(NotificationSeverity.HIGH);
        assertThat(request.getRelatedEntityId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("consume: tipo desconocido → descarta evento sin crear notificación")
    void consume_unknownType_discards() {
        NotificationEvent event = NotificationEvent.builder()
                .userId(1L).type("INVALID_TYPE").title("X").message("X").severity("INFO").build();

        consumer.consume(event);

        verify(createNotificationPort, never()).create(any());
    }

    @Test
    @DisplayName("consume: severidad desconocida → descarta evento")
    void consume_unknownSeverity_discards() {
        NotificationEvent event = NotificationEvent.builder()
                .userId(1L).type("TASK_REMINDER").title("X").message("X").severity("EXTREME").build();

        consumer.consume(event);

        verify(createNotificationPort, never()).create(any());
    }

    @Test
    @DisplayName("consume: tipo null → descarta evento")
    void consume_nullType_discards() {
        NotificationEvent event = NotificationEvent.builder()
                .userId(1L).type(null).title("X").message("X").severity("HIGH").build();

        consumer.consume(event);

        verify(createNotificationPort, never()).create(any());
    }

    @Test
    @DisplayName("consume: severidad null → descarta evento")
    void consume_nullSeverity_discards() {
        NotificationEvent event = NotificationEvent.builder()
                .userId(1L).type("TASK_REMINDER").title("X").message("X").severity(null).build();

        consumer.consume(event);

        verify(createNotificationPort, never()).create(any());
    }

    @Test
    @DisplayName("consume: tipo STUDY_SESSION_INVITE con severidad MEDIUM → crea notificación")
    void consume_studySessionInvite_createsNotification() {
        NotificationEvent event = NotificationEvent.builder()
                .userId(5L)
                .type("STUDY_SESSION_INVITE")
                .title("Invitación a sesión")
                .message("Te invitaron a una sesión de estudio.")
                .severity("MEDIUM")
                .relatedEntityId(null)
                .build();

        when(createNotificationPort.create(any())).thenReturn(mock(NotificationResponse.class));

        consumer.consume(event);

        ArgumentCaptor<CreateNotificationRequest> captor =
                ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(createNotificationPort).create(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.STUDY_SESSION_INVITE);
        assertThat(captor.getValue().getSeverity()).isEqualTo(NotificationSeverity.MEDIUM);
    }
}
