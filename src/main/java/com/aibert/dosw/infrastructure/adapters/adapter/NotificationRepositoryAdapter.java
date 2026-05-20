package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.domain.model.notification.Notification;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.out.NotificationRepositoryPort;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.NotificationEntity;
import com.aibert.dosw.infrastructure.adapters.persistence.mapper.NotificationEntityMapper;
import com.aibert.dosw.infrastructure.adapters.persistence.repository.NotificationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationEntityMapper mapper;

    @Override
    public Notification save(Notification notification) {
        NotificationEntity entity = mapper.toEntity(notification);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Notification> findByUserId(UUID userId) {
        return mapper.toDomainList(jpaRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @Override
    public List<Notification> findByUserIdAndType(UUID userId, NotificationType type) {
        return mapper.toDomainList(
                jpaRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type));
    }

    @Override
    public List<Notification> findUnreadByUserId(UUID userId) {
        return mapper.toDomainList(
                jpaRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId));
    }

    @Override
    public List<Notification> findByUserIdAndTypeInAndCreatedAtAfter(
            UUID userId, List<NotificationType> types, LocalDateTime after) {
        return mapper.toDomainList(
                jpaRepository.findByUserIdAndTypeInAndCreatedAtAfterOrderByCreatedAtDesc(
                        userId, types, after));
    }

    @Override
    public List<Notification> findByUserIdAndTypeAndCreatedAtAfter(
            UUID userId, NotificationType type, LocalDateTime after) {
        return mapper.toDomainList(
                jpaRepository.findByUserIdAndTypeAndCreatedAtAfterOrderByCreatedAtDesc(
                        userId, type, after));
    }

    @Override
    public long countUnreadByUserId(UUID userId) {
        return jpaRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    public void markAsRead(UUID id, LocalDateTime readAt) {
        jpaRepository.markAsRead(id, readAt);
    }

    @Override
    public void markAllAsRead(UUID userId, LocalDateTime readAt) {
        jpaRepository.markAllAsRead(userId, readAt);
    }
}
