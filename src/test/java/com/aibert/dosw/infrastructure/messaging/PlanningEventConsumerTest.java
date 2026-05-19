package com.aibert.dosw.infrastructure.messaging;

import com.aibert.dosw.application.dto.request.CreateNotificationRequest;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.CreateNotificationPort;
import com.aibert.dosw.infrastructure.messaging.event.PlanningEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlanningEventConsumer")
class PlanningEventConsumerTest {

    @Mock private CreateNotificationPort createNotificationPort;

    @InjectMocks
    private PlanningEventConsumer consumer;

    @Test
    @DisplayName("STUDY_SUGGESTION event → creates notification with INFO severity")
    void studySuggestionEvent_createsNotification() {
        PlanningEvent event = PlanningEvent.builder()
                .userId(1L).type("STUDY_SUGGESTION").title("Study Algebra")
                .message("Priority 0.83").severity("INFO").relatedEntityId(5L)
                .build();

        consumer.consume(event);

        ArgumentCaptor<CreateNotificationRequest> captor =
                ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(createNotificationPort).create(captor.capture());

        CreateNotificationRequest req = captor.getValue();
        assertThat(req.getUserId()).isEqualTo(1L);
        assertThat(req.getType()).isEqualTo(NotificationType.STUDY_SUGGESTION);
        assertThat(req.getSeverity()).isEqualTo(NotificationSeverity.INFO);
        assertThat(req.getRelatedEntityId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("OVERLOAD_ALERT event from planning → creates notification")
    void overloadAlertEvent_createsNotification() {
        PlanningEvent event = PlanningEvent.builder()
                .userId(2L).type("OVERLOAD_ALERT").title("Overloaded schedule")
                .message("Reduce tasks").severity("HIGH").relatedEntityId(null)
                .build();

        consumer.consume(event);

        ArgumentCaptor<CreateNotificationRequest> captor =
                ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(createNotificationPort).create(captor.capture());

        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.OVERLOAD_ALERT);
        assertThat(captor.getValue().getSeverity()).isEqualTo(NotificationSeverity.HIGH);
    }

    static Stream<Arguments> invalidEvents() {
        return Stream.of(
                Arguments.of("TASK_REMINDER", "INFO",  "unallowed type for planning.events"),
                Arguments.of(null,            "INFO",  "null type"),
                Arguments.of("STUDY_SUGGESTION", null, "null severity"),
                Arguments.of("STUDY_SUGGESTION", "BAD","unknown severity")
        );
    }

    @ParameterizedTest(name = "{2} → event discarded")
    @MethodSource("invalidEvents")
    @DisplayName("invalid events → no notification created")
    void invalidEvent_eventDiscarded(String type, String severity, String scenario) {
        PlanningEvent event = PlanningEvent.builder()
                .userId(1L).type(type).title("title").message("msg").severity(severity)
                .build();

        consumer.consume(event);

        verify(createNotificationPort, never()).create(any());
    }
}
