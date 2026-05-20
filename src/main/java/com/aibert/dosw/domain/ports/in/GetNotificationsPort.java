package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.response.NotificationResponse;
import com.aibert.dosw.application.dto.response.UnreadCountResponse;
import com.aibert.dosw.domain.model.notification.NotificationType;

import java.util.List;
import java.util.UUID;

public interface GetNotificationsPort {
    List<NotificationResponse> getByUser(UUID userId);
    List<NotificationResponse> getUnreadByUser(UUID userId);
    List<NotificationResponse> getByUserAndType(UUID userId, NotificationType type);
    UnreadCountResponse countUnread(UUID userId);
}
