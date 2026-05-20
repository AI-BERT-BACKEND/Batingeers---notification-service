package com.aibert.dosw.application.dto.response;

import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full notification object returned after creation or retrieval")
public class NotificationResponse {

    @Schema(description = "Unique notification identifier", example = "1")
    private Long id;

    @Schema(description = "ID of the recipient user", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private UUID userId;

    @Schema(description = "Notification category — determines how the app renders the item",
            example = "OVERLOAD_ALERT")
    private NotificationType type;

    @Schema(description = "Short title shown in the notification banner", example = "Academic overload alert")
    private String title;

    @Schema(description = "Full descriptive message body",
            example = "You have 7 active tasks (5 urgent, 2 overdue). Consider redistributing your workload.")
    private String message;

    @Schema(description = "Whether the user has already read this notification", example = "false")
    private boolean read;

    @Schema(description = "Severity level that controls visual styling", example = "HIGH")
    private NotificationSeverity severity;

    @Schema(description = "ID of the related entity for deep-linking (task, subject, etc.)", example = "42")
    private Long relatedEntityId;

    @Schema(description = "UTC timestamp when the notification was created")
    private LocalDateTime createdAt;

    @Schema(description = "UTC timestamp when the user marked this notification as read — null if unread")
    private LocalDateTime readAt;
}
