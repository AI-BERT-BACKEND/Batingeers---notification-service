package com.aibert.dosw.infrastructure.messaging;

import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
}
