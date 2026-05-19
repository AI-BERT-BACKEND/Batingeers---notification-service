package com.aibert.dosw.infrastructure.messaging;

import com.aibert.dosw.application.dto.request.CreateNotificationRequest;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.CreateNotificationPort;
import com.aibert.dosw.infrastructure.messaging.event.AcademicEvent;
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
@DisplayName("AcademicEventConsumer")
class AcademicEventConsumerTest {

    @Mock private CreateNotificationPort createNotificationPort;

    @InjectMocks
    private AcademicEventConsumer consumer;

    @Test
    @DisplayName("valid academic event → creates LOW_PERFORMANCE_ALERT notification")
    void validEvent_createsLowPerformanceAlert() {
        AcademicEvent event = AcademicEvent.builder()
                .userId(1L).type("LOW_PERFORMANCE_ALERT")
                .title("Subjects at risk").message("Grade below 3.0").severity("HIGH")
                .build();

        consumer.consume(event);

        ArgumentCaptor<CreateNotificationRequest> captor =
                ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(createNotificationPort).create(captor.capture());

        CreateNotificationRequest req = captor.getValue();
        assertThat(req.getUserId()).isEqualTo(1L);
        assertThat(req.getType()).isEqualTo(NotificationType.LOW_PERFORMANCE_ALERT);
        assertThat(req.getSeverity()).isEqualTo(NotificationSeverity.HIGH);
        assertThat(req.getTitle()).isEqualTo("Subjects at risk");
    }

    @Test
    @DisplayName("MEDIUM severity → creates notification with MEDIUM severity")
    void mediumSeverity_createsNotification() {
        AcademicEvent event = AcademicEvent.builder()
                .userId(2L).type("LOW_PERFORMANCE_ALERT")
                .title("Warning").message("Grade borderline").severity("MEDIUM")
                .build();

        consumer.consume(event);

        ArgumentCaptor<CreateNotificationRequest> captor =
                ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(createNotificationPort).create(captor.capture());

        assertThat(captor.getValue().getSeverity()).isEqualTo(NotificationSeverity.MEDIUM);
    }

    @Test
    @DisplayName("null severity → event discarded")
    void nullSeverity_eventDiscarded() {
        AcademicEvent event = AcademicEvent.builder()
                .userId(1L).type("LOW_PERFORMANCE_ALERT")
                .title("title").message("msg").severity(null)
                .build();

        consumer.consume(event);

        verify(createNotificationPort, never()).create(any());
    }

    @Test
    @DisplayName("unknown severity → event discarded")
    void unknownSeverity_eventDiscarded() {
        AcademicEvent event = AcademicEvent.builder()
                .userId(1L).type("LOW_PERFORMANCE_ALERT")
                .title("title").message("msg").severity("INVALID")
                .build();

        consumer.consume(event);

        verify(createNotificationPort, never()).create(any());
    }
}
