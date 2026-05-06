package com.aibert.dosw.application.usecase.notification;

import com.aibert.dosw.application.dto.request.CreateNotificationRequest;
import com.aibert.dosw.application.dto.response.NotificationResponse;
import com.aibert.dosw.application.mapper.NotificationMapper;
import com.aibert.dosw.domain.model.notification.Notification;
import com.aibert.dosw.domain.ports.in.CreateNotificationPort;
import com.aibert.dosw.domain.ports.out.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateNotificationUseCase implements CreateNotificationPort {

    private final NotificationRepositoryPort repository;
    private final NotificationMapper mapper;

    @Override
    @Transactional
    public NotificationResponse create(CreateNotificationRequest request) {
        Notification notification = mapper.toDomain(request);
        notification.setCreatedAt(LocalDateTime.now());
        Notification saved = repository.save(notification);
        return mapper.toResponse(saved);
    }
}
