package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.request.CreateNotificationRequest;
import com.aibert.dosw.application.dto.response.NotificationResponse;

public interface CreateNotificationPort {
    NotificationResponse create(CreateNotificationRequest request);
}
