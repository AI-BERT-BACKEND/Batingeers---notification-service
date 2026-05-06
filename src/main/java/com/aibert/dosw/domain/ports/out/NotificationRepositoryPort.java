package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.domain.model.notification.Notification;
import com.aibert.dosw.domain.model.notification.NotificationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepositoryPort {
    Notification save(Notification notification);
    Optional<Notification> findById(Long id);
    List<Notification> findByUserId(Long userId);
    List<Notification> findByUserIdAndType(Long userId, NotificationType type);
    List<Notification> findUnreadByUserId(Long userId);
    List<Notification> findByUserIdAndTypeInAndCreatedAtAfter(Long userId, List<NotificationType> types, LocalDateTime after);
    List<Notification> findByUserIdAndTypeAndCreatedAtAfter(Long userId, NotificationType type, LocalDateTime after);
    long countUnreadByUserId(Long userId);
    void markAsRead(Long id, LocalDateTime readAt);
    void markAllAsRead(Long userId, LocalDateTime readAt);
}
