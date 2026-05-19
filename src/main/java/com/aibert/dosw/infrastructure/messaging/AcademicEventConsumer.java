package com.aibert.dosw.infrastructure.messaging;

import com.aibert.dosw.application.dto.request.CreateNotificationRequest;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.CreateNotificationPort;
import com.aibert.dosw.infrastructure.messaging.event.AcademicEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.aibert.dosw.infrastructure.messaging.EventConsumerUtils.parseSeverity;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcademicEventConsumer {

    static final String TOPIC = "academic.events";

    private final CreateNotificationPort createNotificationPort;

    @KafkaListener(topics = TOPIC, groupId = "notification-service")
    public void consume(AcademicEvent event) {
        log.info("Kafka academic event received: type={} userId={}", event.getType(), event.getUserId());

        NotificationSeverity severity = parseSeverity(event.getSeverity());

        if (severity == null) {
            log.warn("Discarding academic event with unknown severity={}", event.getSeverity());
            return;
        }

        createNotificationPort.create(CreateNotificationRequest.builder()
                .userId(event.getUserId())
                .type(NotificationType.LOW_PERFORMANCE_ALERT)
                .title(event.getTitle())
                .message(event.getMessage())
                .severity(severity)
                .build());

        log.info("LOW_PERFORMANCE_ALERT persisted from academic.events: userId={}", event.getUserId());
    }

}
