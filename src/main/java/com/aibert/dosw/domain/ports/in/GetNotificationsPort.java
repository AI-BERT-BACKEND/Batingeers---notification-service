package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.response.NotificationResponse;
import com.aibert.dosw.application.dto.response.UnreadCountResponse;
import com.aibert.dosw.domain.model.notification.NotificationType;

import java.util.List;

public interface GetNotificationsPort {
    List<NotificationResponse> getByUser(Long userId);
    List<NotificationResponse> getUnreadByUser(Long userId);
    List<NotificationResponse> getByUserAndType(Long userId, NotificationType type);
    UnreadCountResponse countUnread(Long userId);
}
