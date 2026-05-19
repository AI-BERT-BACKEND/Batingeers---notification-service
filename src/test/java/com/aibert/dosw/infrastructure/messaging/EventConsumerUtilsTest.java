package com.aibert.dosw.infrastructure.messaging;

import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EventConsumerUtils")
class EventConsumerUtilsTest {

    @Test
    @DisplayName("valid severity string (uppercase) → returns enum value")
    void validUppercase_returnsSeverity() {
        assertThat(EventConsumerUtils.parseSeverity("HIGH")).isEqualTo(NotificationSeverity.HIGH);
        assertThat(EventConsumerUtils.parseSeverity("MEDIUM")).isEqualTo(NotificationSeverity.MEDIUM);
        assertThat(EventConsumerUtils.parseSeverity("LOW")).isEqualTo(NotificationSeverity.LOW);
        assertThat(EventConsumerUtils.parseSeverity("INFO")).isEqualTo(NotificationSeverity.INFO);
    }

    @Test
    @DisplayName("valid severity string (lowercase) → returns enum value")
    void validLowercase_returnsSeverity() {
        assertThat(EventConsumerUtils.parseSeverity("high")).isEqualTo(NotificationSeverity.HIGH);
        assertThat(EventConsumerUtils.parseSeverity("medium")).isEqualTo(NotificationSeverity.MEDIUM);
    }

    @Test
    @DisplayName("null input → returns null")
    void nullInput_returnsNull() {
        assertThat(EventConsumerUtils.parseSeverity(null)).isNull();
    }

    @Test
    @DisplayName("unknown severity string → returns null")
    void unknownString_returnsNull() {
        assertThat(EventConsumerUtils.parseSeverity("EXTREME")).isNull();
        assertThat(EventConsumerUtils.parseSeverity("CRITICAL")).isNull();
        assertThat(EventConsumerUtils.parseSeverity("")).isNull();
    }
}
