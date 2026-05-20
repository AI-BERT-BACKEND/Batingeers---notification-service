package com.aibert.dosw.application.usecase.notification;

import com.aibert.dosw.application.dto.response.NotificationResponse;
import com.aibert.dosw.application.mapper.NotificationMapper;
import com.aibert.dosw.domain.exceptions.InvalidNotificationException;
import com.aibert.dosw.domain.exceptions.NotificationNotFoundException;
import com.aibert.dosw.domain.model.notification.Notification;
import com.aibert.dosw.domain.ports.in.MarkNotificationReadPort;
import com.aibert.dosw.domain.ports.out.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MarkNotificationReadUseCase implements MarkNotificationReadPort {

    private final NotificationRepositoryPort repository;
    private final NotificationMapper mapper;

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, UUID userId) {
        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new InvalidNotificationException(
                    "Notification does not belong to user with id: " + userId);
        }

        LocalDateTime now = LocalDateTime.now();
        repository.markAsRead(notificationId, now);

        notification.setRead(true);
        notification.setReadAt(now);
        return mapper.toResponse(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        repository.markAllAsRead(userId, LocalDateTime.now());
    }
}
