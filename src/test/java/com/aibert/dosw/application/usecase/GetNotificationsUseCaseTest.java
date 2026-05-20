package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.response.NotificationResponse;
import com.aibert.dosw.application.dto.response.UnreadCountResponse;
import com.aibert.dosw.application.mapper.NotificationMapper;
import com.aibert.dosw.application.usecase.notification.GetNotificationsUseCase;
import com.aibert.dosw.domain.model.notification.Notification;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.out.NotificationRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetNotificationsUseCase")
class GetNotificationsUseCaseTest {

    @Mock
    private NotificationRepositoryPort repository;

    @Mock
    private NotificationMapper mapper;

    @InjectMocks
    private GetNotificationsUseCase useCase;

    private final UUID userId    = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID NOTIF_ID_1 = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    private static final UUID NOTIF_ID_2 = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000002");

    @Test
    @DisplayName("getByUser debe retornar todas las notificaciones del usuario")
    void shouldReturnAllNotificationsForUser() {
        List<Notification> notifications = List.of(
                buildNotification(NOTIF_ID_1, false),
                buildNotification(NOTIF_ID_2, true)
        );
        List<NotificationResponse> responses = List.of(
                buildResponse(NOTIF_ID_1, false),
                buildResponse(NOTIF_ID_2, true)
        );

        when(repository.findByUserId(userId)).thenReturn(notifications);
        when(mapper.toResponseList(notifications)).thenReturn(responses);

        List<NotificationResponse> result = useCase.getByUser(userId);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getUnreadByUser debe retornar solo notificaciones no leídas")
    void shouldReturnOnlyUnreadNotifications() {
        List<Notification> unread = List.of(buildNotification(NOTIF_ID_1, false));
        List<NotificationResponse> responses = List.of(buildResponse(NOTIF_ID_1, false));

        when(repository.findUnreadByUserId(userId)).thenReturn(unread);
        when(mapper.toResponseList(unread)).thenReturn(responses);

        List<NotificationResponse> result = useCase.getUnreadByUser(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isRead()).isFalse();
    }

    @Test
    @DisplayName("countUnread debe retornar el conteo correcto")
    void shouldReturnCorrectUnreadCount() {
        when(repository.countUnreadByUserId(userId)).thenReturn(3L);

        UnreadCountResponse result = useCase.countUnread(userId);

        assertThat(result.getCount()).isEqualTo(3L);
        assertThat(result.getUserId()).isEqualTo(userId);
    }

    private Notification buildNotification(UUID id, boolean read) {
        return Notification.builder()
                .id(id)
                .userId(userId)
                .type(NotificationType.OVERLOAD_ALERT)
                .title("Test")
                .message("Test message")
                .severity(NotificationSeverity.MEDIUM)
                .read(read)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private NotificationResponse buildResponse(UUID id, boolean read) {
        return NotificationResponse.builder()
                .id(id)
                .userId(userId)
                .type(NotificationType.OVERLOAD_ALERT)
                .title("Test")
                .read(read)
                .build();
    }
}
