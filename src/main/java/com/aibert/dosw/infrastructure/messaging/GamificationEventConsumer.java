package com.aibert.dosw.infrastructure.messaging;

import com.aibert.dosw.application.dto.request.CreateNotificationRequest;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.CreateNotificationPort;
import com.aibert.dosw.infrastructure.messaging.event.GamificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GamificationEventConsumer {

    static final String TOPIC = "gamification.events";

    private final CreateNotificationPort createNotificationPort;

    @KafkaListener(topics = TOPIC, groupId = "notification-service")
    public void consume(GamificationEvent event) {
        log.info("Kafka gamification event received: userId={} newLevel={}", event.getUserId(), event.getNewLevelNumber());

        if (event.getUserId() == null) {
            log.warn("Discarding gamification event with null userId");
            return;
        }

        String title = "Level Up! You've reached level " + event.getNewLevelNumber()
                + (event.getLevelName() != null ? ": " + event.getLevelName() : "");
        String message = "Congratulations! You advanced from level " + event.getPreviousLevelNumber()
                + " to level " + event.getNewLevelNumber() + ". Keep it up!";

        createNotificationPort.create(CreateNotificationRequest.builder()
                .userId(event.getUserId())
                .type(NotificationType.LEVEL_UP)
                .title(title)
                .message(message)
                .severity(NotificationSeverity.INFO)
                .build());

        log.info("LEVEL_UP notification persisted from gamification.events: userId={} level={}",
                event.getUserId(), event.getNewLevelNumber());
    }
}
