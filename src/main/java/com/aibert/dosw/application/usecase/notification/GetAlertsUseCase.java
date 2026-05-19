package com.aibert.dosw.application.usecase.notification;

import com.aibert.dosw.application.dto.response.NotificationResponse;
import com.aibert.dosw.application.mapper.NotificationMapper;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.GetAlertsPort;
import com.aibert.dosw.domain.ports.out.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetAlertsUseCase implements GetAlertsPort {

    private final NotificationRepositoryPort repository;
    private final NotificationMapper mapper;

    private static final List<NotificationType> ALERT_TYPES = List.of(
            NotificationType.OVERLOAD_ALERT,
            NotificationType.LOW_PERFORMANCE_ALERT
    );

    @Override
    public List<NotificationResponse> getActiveAlerts(Long userId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return mapper.toResponseList(
                repository.findByUserIdAndTypeInAndCreatedAtAfter(userId, ALERT_TYPES, sevenDaysAgo));
    }
}
