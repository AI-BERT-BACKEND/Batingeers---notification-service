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

import java.util.Set;

import static com.aibert.dosw.infrastructure.messaging.EventConsumerUtils.parseSeverity;
import static com.aibert.dosw.infrastructure.messaging.EventConsumerUtils.parseType;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocialEventConsumer {

    static final String TOPIC = "social.events";

    private final CreateNotificationPort createNotificationPort;

    @KafkaListener(topics = TOPIC, groupId = "notification-service")
    public void consume(SocialEvent event) {
        log.info("Kafka social event received: type={} userId={}", event.getType(), event.getUserId());

        NotificationType type = parseType(event.getType(),
                Set.of(NotificationType.STUDY_SESSION_INVITE,
                       NotificationType.CONNECTION_REQUEST_RECEIVED,
                       NotificationType.CONNECTION_REQUEST_ACCEPTED,
                       NotificationType.NEW_CHAT_MESSAGE), TOPIC);
        NotificationSeverity severity = parseSeverity(event.getSeverity());

        if (type == null || severity == null) {
            log.warn("Discarding social event with unknown type={} or severity={}", event.getType(), event.getSeverity());
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

        log.info("Notification persisted from social.events: type={} userId={}", type, event.getUserId());
    }

}
