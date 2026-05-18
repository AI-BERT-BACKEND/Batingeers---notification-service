package com.aibert.dosw.application.dto.request;

import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to create a new notification from an internal microservice")
public class CreateNotificationRequest {

    @NotNull(message = "userId is required")
    @Positive(message = "userId must be a positive number")
    @Schema(description = "ID of the recipient user", example = "1")
    private Long userId;

    @NotNull(message = "Notification type is required")
    @Schema(description = "Type of notification — controls how it is displayed in the mobile app",
            example = "OVERLOAD_ALERT",
            allowableValues = {"OVERLOAD_ALERT", "LOW_PERFORMANCE_ALERT", "STUDY_SUGGESTION",
                               "TASK_REMINDER", "STUDY_SESSION_INVITE"})
    private NotificationType type;

    @NotBlank(message = "Title is required")
    @Schema(description = "Short title displayed in the notification banner", example = "Academic overload alert")
    private String title;

    @NotBlank(message = "Message is required")
    @Schema(description = "Full message body shown when the notification is expanded",
            example = "You have 7 active tasks (5 urgent, 2 overdue). Consider redistributing your workload.")
    private String message;

    @NotNull(message = "Severity is required")
    @Schema(description = "Severity level that determines the visual style of the notification",
            example = "HIGH",
            allowableValues = {"HIGH", "MEDIUM", "LOW", "INFO"})
    private NotificationSeverity severity;

    @Schema(description = "Optional ID of the related entity (task, subject, etc.) for deep-linking",
            example = "42")
    private Long relatedEntityId;
}
