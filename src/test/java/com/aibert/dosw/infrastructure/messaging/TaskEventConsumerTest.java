package com.aibert.dosw.infrastructure.messaging;

import com.aibert.dosw.application.dto.request.CreateNotificationRequest;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.CreateNotificationPort;
import com.aibert.dosw.infrastructure.messaging.event.TaskEvent;
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
@DisplayName("TaskEventConsumer")
class TaskEventConsumerTest {

    @Mock private CreateNotificationPort createNotificationPort;

    @InjectMocks
    private TaskEventConsumer consumer;

    @Test
    @DisplayName("TASK_REMINDER event → creates notification with correct type and severity")
    void taskReminderEvent_createsNotification() {
        TaskEvent event = TaskEvent.builder()
                .userId(1L).type("TASK_REMINDER").title("Complete Assignment")
                .message("Due tomorrow").severity("MEDIUM").relatedEntityId(10L)
                .build();

        consumer.consume(event);

        ArgumentCaptor<CreateNotificationRequest> captor =
                ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(createNotificationPort).create(captor.capture());

        CreateNotificationRequest req = captor.getValue();
        assertThat(req.getUserId()).isEqualTo(1L);
        assertThat(req.getType()).isEqualTo(NotificationType.TASK_REMINDER);
        assertThat(req.getSeverity()).isEqualTo(NotificationSeverity.MEDIUM);
        assertThat(req.getTitle()).isEqualTo("Complete Assignment");
        assertThat(req.getRelatedEntityId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("OVERLOAD_ALERT event → creates notification with HIGH severity")
    void overloadAlertEvent_createsNotification() {
        TaskEvent event = TaskEvent.builder()
                .userId(2L).type("OVERLOAD_ALERT").title("Too many tasks")
                .message("Reduce workload").severity("HIGH").relatedEntityId(null)
                .build();

        consumer.consume(event);

        ArgumentCaptor<CreateNotificationRequest> captor =
                ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(createNotificationPort).create(captor.capture());

        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.OVERLOAD_ALERT);
        assertThat(captor.getValue().getSeverity()).isEqualTo(NotificationSeverity.HIGH);
    }

    @Test
    @DisplayName("type=STUDY_SUGGESTION (not allowed for task.events) → event discarded")
    void unallowedType_eventDiscarded() {
        TaskEvent event = TaskEvent.builder()
                .userId(1L).type("STUDY_SUGGESTION").title("Study Math")
                .message("msg").severity("INFO")
                .build();

        consumer.consume(event);

        verify(createNotificationPort, never()).create(any());
    }

    @Test
    @DisplayName("null type → event discarded")
    void nullType_eventDiscarded() {
        TaskEvent event = TaskEvent.builder()
                .userId(1L).type(null).title("title").message("msg").severity("HIGH")
                .build();

        consumer.consume(event);

        verify(createNotificationPort, never()).create(any());
    }

    @Test
    @DisplayName("unknown severity → event discarded")
    void unknownSeverity_eventDiscarded() {
        TaskEvent event = TaskEvent.builder()
                .userId(1L).type("TASK_REMINDER").title("title").message("msg").severity("UNKNOWN")
                .build();

        consumer.consume(event);

        verify(createNotificationPort, never()).create(any());
    }

    @Test
    @DisplayName("null severity → event discarded")
    void nullSeverity_eventDiscarded() {
        TaskEvent event = TaskEvent.builder()
                .userId(1L).type("TASK_REMINDER").title("title").message("msg").severity(null)
                .build();

        consumer.consume(event);

        verify(createNotificationPort, never()).create(any());
    }
}
