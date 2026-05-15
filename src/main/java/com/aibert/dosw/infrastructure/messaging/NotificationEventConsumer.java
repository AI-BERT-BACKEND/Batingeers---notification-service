package com.aibert.dosw.infrastructure.messaging;

import com.aibert.dosw.application.dto.request.CreateNotificationRequest;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.CreateNotificationPort;
import com.aibert.dosw.infrastructure.messaging.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    static final String TOPIC_NOTIFICATION_EVENTS = "notification-events";

    private final CreateNotificationPort createNotificationPort;

    @KafkaListener(topics = TOPIC_NOTIFICATION_EVENTS, groupId = "notification-service")
    public void consume(NotificationEvent event) {
        log.info("Kafka event received: type={} userId={}", event.getType(), event.getUserId());

        NotificationType type = parseType(event.getType());
        NotificationSeverity severity = parseSeverity(event.getSeverity());

        if (type == null || severity == null) {
            log.warn("Discarding event with unknown type={} or severity={}", event.getType(), event.getSeverity());
            return;
        }

        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(event.getUserId())
                .type(type)
                .title(event.getTitle())
                .message(event.getMessage())
                .severity(severity)
                .relatedEntityId(event.getRelatedEntityId())
                .build();

        createNotificationPort.create(request);
        log.info("Notification persisted from Kafka event: type={} userId={}", type, event.getUserId());
    }

    private NotificationType parseType(String raw) {
        if (raw == null) return null;
        try {
            return NotificationType.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private NotificationSeverity parseSeverity(String raw) {
        if (raw == null) return null;
        try {
            return NotificationSeverity.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
