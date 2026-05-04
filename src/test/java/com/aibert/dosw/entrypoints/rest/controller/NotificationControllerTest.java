package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.request.CreateNotificationRequest;
import com.aibert.dosw.application.dto.response.NotificationResponse;
import com.aibert.dosw.application.dto.response.UnreadCountResponse;
import com.aibert.dosw.config.JwtAuthenticationFilter;
import com.aibert.dosw.config.UserPrincipal;
import com.aibert.dosw.domain.exceptions.NotificationNotFoundException;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.CreateNotificationPort;
import com.aibert.dosw.domain.ports.in.GetAlertsPort;
import com.aibert.dosw.domain.ports.in.GetNotificationsPort;
import com.aibert.dosw.domain.ports.in.GetStudySuggestionsPort;
import com.aibert.dosw.domain.ports.in.MarkNotificationReadPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = NotificationController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@DisplayName("NotificationController")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateNotificationPort createNotificationPort;

    @MockBean
    private GetNotificationsPort getNotificationsPort;

    @MockBean
    private MarkNotificationReadPort markNotificationReadPort;

    @MockBean
    private GetStudySuggestionsPort getStudySuggestionsPort;

    @MockBean
    private GetAlertsPort getAlertsPort;

    private final Long userId = 1L;

    private UsernamePasswordAuthenticationToken auth() {
        UserPrincipal principal = new UserPrincipal(userId, "testuser");
        return new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    }

    @Test
    @DisplayName("POST /api/v1/notifications debe crear notificación y retornar 201")
    void shouldCreateNotificationAndReturn201() throws Exception {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.OVERLOAD_ALERT)
                .title("Sobrecarga")
                .message("Tienes demasiadas tareas asignadas")
                .severity(NotificationSeverity.HIGH)
                .build();

        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .userId(userId)
                .type(NotificationType.OVERLOAD_ALERT)
                .title("Sobrecarga")
                .severity(NotificationSeverity.HIGH)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(createNotificationPort.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications")
                        .with(csrf())
                        .with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.type").value("OVERLOAD_ALERT"))
                .andExpect(jsonPath("$.read").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/notifications debe retornar 400 si faltan campos obligatorios")
    void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
        CreateNotificationRequest invalid = CreateNotificationRequest.builder()
                .userId(userId)
                .build();

        mockMvc.perform(post("/api/v1/notifications")
                        .with(csrf())
                        .with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/notifications/me debe retornar lista de notificaciones")
    void shouldReturnUserNotifications() throws Exception {
        List<NotificationResponse> notifications = List.of(
                NotificationResponse.builder().id(1L).userId(userId)
                        .type(NotificationType.STUDY_SUGGESTION).read(false).build()
        );

        when(getNotificationsPort.getByUser(userId)).thenReturn(notifications);

        mockMvc.perform(get("/api/v1/notifications/me")
                        .with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("STUDY_SUGGESTION"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/me/unread debe retornar solo no leídas")
    void shouldReturnOnlyUnreadNotifications() throws Exception {
        List<NotificationResponse> unread = List.of(
                NotificationResponse.builder().id(2L).userId(userId)
                        .type(NotificationType.LOW_PERFORMANCE_ALERT).read(false).build()
        );

        when(getNotificationsPort.getUnreadByUser(userId)).thenReturn(unread);

        mockMvc.perform(get("/api/v1/notifications/me/unread")
                        .with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/me/count debe retornar el conteo")
    void shouldReturnUnreadCount() throws Exception {
        when(getNotificationsPort.countUnread(userId))
                .thenReturn(new UnreadCountResponse(userId, 5L));

        mockMvc.perform(get("/api/v1/notifications/me/count")
                        .with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/me/suggestions debe retornar sugerencias R23")
    void shouldReturnStudySuggestions() throws Exception {
        List<NotificationResponse> suggestions = List.of(
                NotificationResponse.builder().id(3L).userId(userId)
                        .type(NotificationType.STUDY_SUGGESTION)
                        .title("¿Qué estudiar hoy?").read(false).build()
        );

        when(getStudySuggestionsPort.getTodaySuggestions(userId)).thenReturn(suggestions);

        mockMvc.perform(get("/api/v1/notifications/me/suggestions")
                        .with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("STUDY_SUGGESTION"))
                .andExpect(jsonPath("$[0].title").value("¿Qué estudiar hoy?"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/me/alerts debe retornar alertas R22")
    void shouldReturnAlerts() throws Exception {
        List<NotificationResponse> alerts = List.of(
                NotificationResponse.builder().id(4L).userId(userId)
                        .type(NotificationType.OVERLOAD_ALERT)
                        .severity(NotificationSeverity.HIGH).read(false).build()
        );

        when(getAlertsPort.getActiveAlerts(userId)).thenReturn(alerts);

        mockMvc.perform(get("/api/v1/notifications/me/alerts")
                        .with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("OVERLOAD_ALERT"))
                .andExpect(jsonPath("$[0].severity").value("HIGH"));
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/{id}/read debe marcar como leída y retornar 200")
    void shouldMarkNotificationAsRead() throws Exception {
        NotificationResponse marked = NotificationResponse.builder()
                .id(1L).userId(userId).read(true).build();

        when(markNotificationReadPort.markAsRead(eq(1L), eq(userId))).thenReturn(marked);

        mockMvc.perform(put("/api/v1/notifications/1/read")
                        .with(csrf())
                        .with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/{id}/read debe retornar 404 si no existe")
    void shouldReturn404WhenNotificationNotFound() throws Exception {
        when(markNotificationReadPort.markAsRead(eq(99L), eq(userId)))
                .thenThrow(new NotificationNotFoundException(99L));

        mockMvc.perform(put("/api/v1/notifications/99/read")
                        .with(csrf())
                        .with(authentication(auth())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/me/read-all debe retornar 204")
    void shouldMarkAllAsReadAndReturn204() throws Exception {
        doNothing().when(markNotificationReadPort).markAllAsRead(userId);

        mockMvc.perform(put("/api/v1/notifications/me/read-all")
                        .with(csrf())
                        .with(authentication(auth())))
                .andExpect(status().isNoContent());
    }
}
