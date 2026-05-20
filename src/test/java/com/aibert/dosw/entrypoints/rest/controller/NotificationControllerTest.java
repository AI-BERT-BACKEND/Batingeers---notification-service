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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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

    private static final UUID USER_ID     = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID NOTIF_ID_1  = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    private static final UUID NOTIF_ID_2  = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000002");
    private static final UUID NOTIF_ID_3  = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000003");
    private static final UUID NOTIF_ID_4  = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000004");
    private static final UUID NOTIF_ID_99 = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000099");

    private UsernamePasswordAuthenticationToken auth() {
        UserPrincipal principal = new UserPrincipal(USER_ID, "testuser");
        return new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    }

    @Test
    @DisplayName("POST /api/v1/notifications debe crear notificación y retornar 201")
    void shouldCreateNotificationAndReturn201() throws Exception {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(USER_ID)
                .type(NotificationType.OVERLOAD_ALERT)
                .title("Sobrecarga")
                .message("Tienes demasiadas tareas asignadas")
                .severity(NotificationSeverity.HIGH)
                .build();

        NotificationResponse response = NotificationResponse.builder()
                .id(NOTIF_ID_1)
                .userId(USER_ID)
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
                .andExpect(jsonPath("$.id").value(NOTIF_ID_1.toString()))
                .andExpect(jsonPath("$.type").value("OVERLOAD_ALERT"))
                .andExpect(jsonPath("$.read").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/notifications debe retornar 400 si faltan campos obligatorios")
    void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
        CreateNotificationRequest invalid = CreateNotificationRequest.builder()
                .userId(USER_ID)
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
                NotificationResponse.builder().id(NOTIF_ID_1).userId(USER_ID)
                        .type(NotificationType.STUDY_SUGGESTION).read(false).build()
        );

        when(getNotificationsPort.getByUser(USER_ID)).thenReturn(notifications);

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
                NotificationResponse.builder().id(NOTIF_ID_2).userId(USER_ID)
                        .type(NotificationType.LOW_PERFORMANCE_ALERT).read(false).build()
        );

        when(getNotificationsPort.getUnreadByUser(USER_ID)).thenReturn(unread);

        mockMvc.perform(get("/api/v1/notifications/me/unread")
                        .with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/me/count debe retornar el conteo")
    void shouldReturnUnreadCount() throws Exception {
        when(getNotificationsPort.countUnread(USER_ID))
                .thenReturn(new UnreadCountResponse(USER_ID, 5L));

        mockMvc.perform(get("/api/v1/notifications/me/count")
                        .with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/me/suggestions debe retornar sugerencias R23")
    void shouldReturnStudySuggestions() throws Exception {
        List<NotificationResponse> suggestions = List.of(
                NotificationResponse.builder().id(NOTIF_ID_3).userId(USER_ID)
                        .type(NotificationType.STUDY_SUGGESTION)
                        .title("¿Qué estudiar hoy?").read(false).build()
        );

        when(getStudySuggestionsPort.getTodaySuggestions(USER_ID)).thenReturn(suggestions);

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
                NotificationResponse.builder().id(NOTIF_ID_4).userId(USER_ID)
                        .type(NotificationType.OVERLOAD_ALERT)
                        .severity(NotificationSeverity.HIGH).read(false).build()
        );

        when(getAlertsPort.getActiveAlerts(USER_ID)).thenReturn(alerts);

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
                .id(NOTIF_ID_1).userId(USER_ID).read(true).build();

        when(markNotificationReadPort.markAsRead(NOTIF_ID_1, USER_ID)).thenReturn(marked);

        mockMvc.perform(put("/api/v1/notifications/" + NOTIF_ID_1 + "/read")
                        .with(csrf())
                        .with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/{id}/read debe retornar 404 si no existe")
    void shouldReturn404WhenNotificationNotFound() throws Exception {
        when(markNotificationReadPort.markAsRead(NOTIF_ID_99, USER_ID))
                .thenThrow(new NotificationNotFoundException(NOTIF_ID_99));

        mockMvc.perform(put("/api/v1/notifications/" + NOTIF_ID_99 + "/read")
                        .with(csrf())
                        .with(authentication(auth())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/me/read-all debe retornar 204")
    void shouldMarkAllAsReadAndReturn204() throws Exception {
        doNothing().when(markNotificationReadPort).markAllAsRead(USER_ID);

        mockMvc.perform(put("/api/v1/notifications/me/read-all")
                        .with(csrf())
                        .with(authentication(auth())))
                .andExpect(status().isNoContent());
    }
}
