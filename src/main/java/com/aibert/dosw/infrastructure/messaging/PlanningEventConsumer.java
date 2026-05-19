package com.aibert.dosw.infrastructure.messaging;

import com.aibert.dosw.application.dto.request.CreateNotificationRequest;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.CreateNotificationPort;
import com.aibert.dosw.infrastructure.messaging.event.PlanningEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanningEventConsumer {

    static final String TOPIC = "planning.events";

    private final CreateNotificationPort createNotificationPort;

    @KafkaListener(topics = TOPIC, groupId = "notification-service")
    public void consume(PlanningEvent event) {
        log.info("Kafka planning event received: type={} userId={}", event.getType(), event.getUserId());

        NotificationType type = parseType(event.getType());
        NotificationSeverity severity = parseSeverity(event.getSeverity());

        if (type == null || severity == null) {
            log.warn("Discarding planning event with unknown type={} or severity={}", event.getType(), event.getSeverity());
            return;
        }

        createNotificationPort.create(CreateNotificationRequest.builder()
                .userId(event.getUserId())
                .type(type)
                .title(event.getTitle())
                .message(event.getMessage())
                .severity(severity)
                .relatedEntityId(event.getRelatedEntityId())
                .build());

        log.info("Notification persisted from planning.events: type={} userId={}", type, event.getUserId());
    }

    private NotificationType parseType(String raw) {
        if (raw == null) return null;
        try {
            NotificationType type = NotificationType.valueOf(raw.toUpperCase());
            if (type == NotificationType.STUDY_SUGGESTION || type == NotificationType.OVERLOAD_ALERT) return type;
            log.warn("Unexpected notification type for planning.events: {}", raw);
            return null;
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
