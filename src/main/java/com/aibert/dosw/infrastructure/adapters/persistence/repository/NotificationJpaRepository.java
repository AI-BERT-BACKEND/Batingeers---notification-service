package com.aibert.dosw.infrastructure.adapters.persistence.repository;

import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID> {

    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<NotificationEntity> findByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, NotificationType type);

    List<NotificationEntity> findByUserIdAndReadFalseOrderByCreatedAtDesc(UUID userId);

    long countByUserIdAndReadFalse(UUID userId);

    List<NotificationEntity> findByUserIdAndTypeInAndCreatedAtAfterOrderByCreatedAtDesc(
            UUID userId, List<NotificationType> types, LocalDateTime after);

    List<NotificationEntity> findByUserIdAndTypeAndCreatedAtAfterOrderByCreatedAtDesc(
            UUID userId, NotificationType type, LocalDateTime after);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.read = true, n.readAt = :readAt WHERE n.id = :id")
    void markAsRead(@Param("id") UUID id, @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.read = true, n.readAt = :readAt WHERE n.userId = :userId AND n.read = false")
    void markAllAsRead(@Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);
}
