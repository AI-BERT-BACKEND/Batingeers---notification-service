package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.response.NotificationResponse;

import java.util.List;

public interface GetAlertsPort {
    List<NotificationResponse> getActiveAlerts(Long userId);
}
