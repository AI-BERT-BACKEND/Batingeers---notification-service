package com.aibert.dosw.application.usecase.notification;

import com.aibert.dosw.application.dto.response.NotificationResponse;
import com.aibert.dosw.application.dto.response.UnreadCountResponse;
import com.aibert.dosw.application.mapper.NotificationMapper;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.GetNotificationsPort;
import com.aibert.dosw.domain.ports.out.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetNotificationsUseCase implements GetNotificationsPort {

    private final NotificationRepositoryPort repository;
    private final NotificationMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getByUser(Long userId) {
        return mapper.toResponseList(repository.findByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadByUser(Long userId) {
        return mapper.toResponseList(repository.findUnreadByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getByUserAndType(Long userId, NotificationType type) {
        return mapper.toResponseList(repository.findByUserIdAndType(userId, type));
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse countUnread(Long userId) {
        long count = repository.countUnreadByUserId(userId);
        return new UnreadCountResponse(userId, count);
    }
}
