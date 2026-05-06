package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.CreateNotificationRequest;
import com.aibert.dosw.application.dto.response.NotificationResponse;
import com.aibert.dosw.application.dto.response.UnreadCountResponse;
import com.aibert.dosw.config.UserPrincipal;
import com.aibert.dosw.domain.ports.in.CreateNotificationPort;
import com.aibert.dosw.domain.ports.in.GetAlertsPort;
import com.aibert.dosw.domain.ports.in.GetNotificationsPort;
import com.aibert.dosw.domain.ports.in.GetStudySuggestionsPort;
import com.aibert.dosw.domain.ports.in.MarkNotificationReadPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Gestión de notificaciones académicas (R22, R23)")
public class NotificationController {

    private final CreateNotificationPort createNotificationPort;
    private final GetNotificationsPort getNotificationsPort;
    private final MarkNotificationReadPort markNotificationReadPort;
    private final GetStudySuggestionsPort getStudySuggestionsPort;
    private final GetAlertsPort getAlertsPort;

    @PostMapping
    @Operation(
        summary = "Crear notificación",
        description = "Endpoint para que otros microservicios (planning-service, academic-service, " +
                      "social-service) envíen notificaciones a un usuario."
    )
    public ResponseEntity<NotificationResponse> create(
            @Valid @RequestBody CreateNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createNotificationPort.create(request));
    }

    @GetMapping("/me")
    @Operation(
        summary = "Mis notificaciones",
        description = "Retorna todas las notificaciones del usuario autenticado, ordenadas por fecha."
    )
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(getNotificationsPort.getByUser(principal.getUserId()));
    }

    @GetMapping("/me/unread")
    @Operation(
        summary = "Notificaciones no leídas",
        description = "Retorna solo las notificaciones no leídas del usuario autenticado."
    )
    public ResponseEntity<List<NotificationResponse>> getMyUnread(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(getNotificationsPort.getUnreadByUser(principal.getUserId()));
    }

    @GetMapping("/me/count")
    @Operation(
        summary = "Cantidad de no leídas",
        description = "Retorna el conteo de notificaciones no leídas del usuario autenticado."
    )
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(getNotificationsPort.countUnread(principal.getUserId()));
    }

    @GetMapping("/me/suggestions")
    @Operation(
        summary = "R23 — ¿Qué estudiar hoy?",
        description = "Retorna las sugerencias de estudio para el día actual. Si no existen, " +
                      "consulta el planning-service para generarlas automáticamente."
    )
    public ResponseEntity<List<NotificationResponse>> getTodaySuggestions(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(getStudySuggestionsPort.getTodaySuggestions(principal.getUserId()));
    }

    @GetMapping("/me/alerts")
    @Operation(
        summary = "R22 — Alertas de sobrecarga y bajo rendimiento",
        description = "Retorna alertas activas de sobrecarga académica y bajo rendimiento. " +
                      "Consulta task-service y academic-service para generar nuevas alertas si aplica."
    )
    public ResponseEntity<List<NotificationResponse>> getMyAlerts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(getAlertsPort.getActiveAlerts(principal.getUserId()));
    }

    @PutMapping("/{id}/read")
    @Operation(
        summary = "Marcar notificación como leída",
        description = "Marca una notificación específica como leída. Verifica que pertenezca al usuario."
    )
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(markNotificationReadPort.markAsRead(id, principal.getUserId()));
    }

    @PutMapping("/me/read-all")
    @Operation(
        summary = "Marcar todas como leídas",
        description = "Marca todas las notificaciones no leídas del usuario como leídas."
    )
    public ResponseEntity<Void> markAllAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        markNotificationReadPort.markAllAsRead(principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
