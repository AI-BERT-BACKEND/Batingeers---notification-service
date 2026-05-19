package com.aibert.dosw.infrastructure.messaging;

import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventConsumerUtils {

    public static NotificationSeverity parseSeverity(String raw) {
        if (raw == null) return null;
        try {
            return NotificationSeverity.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown severity value: {}", raw);
            return null;
        }
    }

    public static NotificationType parseType(String raw, Set<NotificationType> allowed, String topic) {
        if (raw == null) return null;
        try {
            NotificationType type = NotificationType.valueOf(raw.toUpperCase());
            if (allowed.contains(type)) return type;
            log.warn("Unexpected notification type for {}: {}", topic, raw);
            return null;
        } catch (IllegalArgumentException e) {
            log.warn("Unknown notification type for {}: {}", topic, raw);
            return null;
        }
    }
}
