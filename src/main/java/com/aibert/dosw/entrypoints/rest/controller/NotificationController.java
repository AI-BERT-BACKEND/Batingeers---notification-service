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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Manage the notification inbox: create, retrieve, and mark as read (R22, R23)")
public class NotificationController {

    private final CreateNotificationPort createNotificationPort;
    private final GetNotificationsPort getNotificationsPort;
    private final MarkNotificationReadPort markNotificationReadPort;
    private final GetStudySuggestionsPort getStudySuggestionsPort;
    private final GetAlertsPort getAlertsPort;

    @PostMapping
    @Operation(
        summary = "Create a notification (internal microservice endpoint)",
        description = """
            Called by other microservices (task-service, academic-service, planning-service, \
            social-service) to persist a notification for a user. \
            The `type` field controls how the mobile app renders the item. \
            This endpoint does NOT require the caller to be the recipient user.
            """
    )
    @ApiResponse(responseCode = "201", description = "Notification created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error — missing or invalid fields",
                 content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                 content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<NotificationResponse> create(
            @Valid @RequestBody CreateNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createNotificationPort.create(request));
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get all my notifications",
        description = """
            Returns the full notification history for the authenticated user, \
            sorted by creation date descending. Includes read and unread notifications \
            of all types (alerts, suggestions, invitations, reminders).
            """
    )
    @ApiResponse(responseCode = "200", description = "List of notifications returned successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                 content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(getNotificationsPort.getByUser(principal.getUserId()));
    }

    @GetMapping("/me/unread")
    @Operation(
        summary = "Get my unread notifications",
        description = """
            Returns only unread notifications for the authenticated user, \
            sorted by creation date descending. Use this to drive the notification badge count.
            """
    )
    @ApiResponse(responseCode = "200", description = "Unread notifications returned successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                 content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<List<NotificationResponse>> getMyUnread(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(getNotificationsPort.getUnreadByUser(principal.getUserId()));
    }

    @GetMapping("/me/count")
    @Operation(
        summary = "Get unread notification count",
        description = """
            Returns the total number of unread notifications for the authenticated user. \
            Designed for lightweight polling to update the app notification badge without \
            fetching the full notification list.
            """
    )
    @ApiResponse(responseCode = "200", description = "Unread count returned successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                 content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(getNotificationsPort.countUnread(principal.getUserId()));
    }

    @GetMapping("/me/suggestions")
    @Operation(
        summary = "R23 — Get today's study suggestions",
        description = """
            Returns study suggestion notifications generated for the current day. \
            If no suggestion exists yet, the service queries planning-service for today's plan \
            and persists a new STUDY_SUGGESTION notification before returning it. \
            Returns an empty list if no pending tasks are found.
            """
    )
    @ApiResponse(responseCode = "200", description = "Study suggestions returned (may be empty)")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                 content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<List<NotificationResponse>> getTodaySuggestions(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(getStudySuggestionsPort.getTodaySuggestions(principal.getUserId()));
    }

    @GetMapping("/me/alerts")
    @Operation(
        summary = "R22 — Get active alert notifications",
        description = """
            Returns overload and low-performance alert notifications created in the last 7 days. \
            If no alert was generated today, the service evaluates current workload and \
            academic performance in real time and persists a new notification if warranted. \
            Returns an empty list if no alerts are triggered.
            """
    )
    @ApiResponse(responseCode = "200", description = "Alert notifications returned (may be empty)")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                 content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<List<NotificationResponse>> getMyAlerts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(getAlertsPort.getActiveAlerts(principal.getUserId()));
    }

    @PutMapping("/{id}/read")
    @Operation(
        summary = "Mark a notification as read",
        description = """
            Marks a single notification as read and records the read timestamp. \
            Returns 403 if the notification belongs to a different user.
            """
    )
    @ApiResponse(responseCode = "200", description = "Notification marked as read")
    @ApiResponse(responseCode = "403", description = "Notification does not belong to the authenticated user",
                 content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "404", description = "Notification not found",
                 content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(markNotificationReadPort.markAsRead(id, principal.getUserId()));
    }

    @PutMapping("/me/read-all")
    @Operation(
        summary = "Mark all notifications as read",
        description = """
            Marks every unread notification for the authenticated user as read in a single operation. \
            Returns 204 No Content on success.
            """
    )
    @ApiResponse(responseCode = "204", description = "All notifications marked as read")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                 content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<Void> markAllAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        markNotificationReadPort.markAllAsRead(principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
