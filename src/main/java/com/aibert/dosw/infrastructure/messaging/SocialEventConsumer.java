package com.aibert.dosw.infrastructure.messaging;

import com.aibert.dosw.application.dto.request.CreateNotificationRequest;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.CreateNotificationPort;
import com.aibert.dosw.infrastructure.messaging.event.SocialEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocialEventConsumer {

    static final String TOPIC = "social.events";

    private final CreateNotificationPort createNotificationPort;

    @KafkaListener(topics = TOPIC, groupId = "notification-service")
    public void consume(SocialEvent event) {
        log.info("Kafka social event received: type={} userId={}", event.getType(), event.getUserId());

        NotificationSeverity severity = parseSeverity(event.getSeverity());

        if (severity == null) {
            log.warn("Discarding social event with unknown severity={}", event.getSeverity());
            return;
        }

        createNotificationPort.create(CreateNotificationRequest.builder()
                .userId(event.getUserId())
                .type(NotificationType.STUDY_SESSION_INVITE)
                .title(event.getTitle())
                .message(event.getMessage())
                .severity(severity)
                .relatedEntityId(event.getRelatedEntityId())
                .build());

        log.info("STUDY_SESSION_INVITE persisted from social.events: userId={}", event.getUserId());
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
