package com.aibert.dosw.application.dto.response;

import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con los datos de una notificación")
public class NotificationResponse {

    @Schema(description = "ID de la notificación", example = "1")
    private Long id;

    @Schema(description = "ID del usuario destinatario", example = "1")
    private Long userId;

    @Schema(description = "Tipo de notificación", example = "OVERLOAD_ALERT")
    private NotificationType type;

    @Schema(description = "Título de la notificación", example = "Sobrecarga detectada")
    private String title;

    @Schema(description = "Mensaje de la notificación")
    private String message;

    @Schema(description = "Indica si fue leída", example = "false")
    private boolean read;

    @Schema(description = "Severidad", example = "HIGH")
    private NotificationSeverity severity;

    @Schema(description = "ID de la entidad relacionada", example = "42")
    private Long relatedEntityId;

    @Schema(description = "Fecha y hora de creación")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha y hora en que fue leída")
    private LocalDateTime readAt;
}
