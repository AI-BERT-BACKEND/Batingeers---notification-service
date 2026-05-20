package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.domain.model.notification.Notification;
import com.aibert.dosw.domain.model.notification.NotificationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepositoryPort {
    Notification save(Notification notification);
    Optional<Notification> findById(UUID id);
    List<Notification> findByUserId(UUID userId);
    List<Notification> findByUserIdAndType(UUID userId, NotificationType type);
    List<Notification> findUnreadByUserId(UUID userId);
    List<Notification> findByUserIdAndTypeInAndCreatedAtAfter(UUID userId, List<NotificationType> types, LocalDateTime after);
    List<Notification> findByUserIdAndTypeAndCreatedAtAfter(UUID userId, NotificationType type, LocalDateTime after);
    long countUnreadByUserId(UUID userId);
    void markAsRead(UUID id, LocalDateTime readAt);
    void markAllAsRead(UUID userId, LocalDateTime readAt);
}
