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
@Schema(description = "Solicitud para crear una nueva notificación")
public class CreateNotificationRequest {

    @NotNull(message = "El userId es obligatorio")
    @Positive(message = "El userId debe ser positivo")
    @Schema(description = "ID del usuario destinatario", example = "1")
    private Long userId;

    @NotNull(message = "El tipo de notificación es obligatorio")
    @Schema(description = "Tipo de notificación", example = "OVERLOAD_ALERT")
    private NotificationType type;

    @NotBlank(message = "El título es obligatorio")
    @Schema(description = "Título de la notificación", example = "Sobrecarga detectada")
    private String title;

    @NotBlank(message = "El mensaje es obligatorio")
    @Schema(description = "Cuerpo del mensaje", example = "Tienes más de 5 tareas urgentes para hoy.")
    private String message;

    @NotNull(message = "La severidad es obligatoria")
    @Schema(description = "Severidad de la notificación", example = "HIGH")
    private NotificationSeverity severity;

    @Schema(description = "ID de la entidad relacionada (tarea, materia, etc.)", example = "42")
    private Long relatedEntityId;
}
