package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.response.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface GetAlertsPort {
    List<NotificationResponse> getActiveAlerts(UUID userId);
}
