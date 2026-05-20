package com.aibert.dosw.domain.model.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    private UUID id;
    private UUID userId;
    private NotificationType type;
    private String title;
    private String message;
    private boolean read;
    private NotificationSeverity severity;
    private UUID relatedEntityId;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
