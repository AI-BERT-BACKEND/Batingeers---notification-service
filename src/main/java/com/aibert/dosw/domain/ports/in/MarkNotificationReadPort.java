package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.response.NotificationResponse;

public interface MarkNotificationReadPort {
    NotificationResponse markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
}
