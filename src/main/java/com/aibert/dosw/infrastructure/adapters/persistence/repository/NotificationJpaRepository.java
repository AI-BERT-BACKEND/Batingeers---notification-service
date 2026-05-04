package com.aibert.dosw.infrastructure.adapters.persistence.repository;

import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<NotificationEntity> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, NotificationType type);

    List<NotificationEntity> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndReadFalse(Long userId);

    List<NotificationEntity> findByUserIdAndTypeInAndCreatedAtAfterOrderByCreatedAtDesc(
            Long userId, List<NotificationType> types, LocalDateTime after);

    List<NotificationEntity> findByUserIdAndTypeAndCreatedAtAfterOrderByCreatedAtDesc(
            Long userId, NotificationType type, LocalDateTime after);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.read = true, n.readAt = :readAt WHERE n.id = :id")
    void markAsRead(@Param("id") Long id, @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.read = true, n.readAt = :readAt WHERE n.userId = :userId AND n.read = false")
    void markAllAsRead(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);
}
