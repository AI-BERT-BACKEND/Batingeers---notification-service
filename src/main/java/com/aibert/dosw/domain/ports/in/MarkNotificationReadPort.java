package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.response.NotificationResponse;
import java.util.UUID;

public interface MarkNotificationReadPort {
    NotificationResponse markAsRead(UUID notificationId, UUID userId);
    void markAllAsRead(UUID userId);
}
