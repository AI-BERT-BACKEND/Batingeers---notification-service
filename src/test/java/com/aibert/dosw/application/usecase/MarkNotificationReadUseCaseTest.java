package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.response.NotificationResponse;
import com.aibert.dosw.application.mapper.NotificationMapper;
import com.aibert.dosw.application.usecase.notification.MarkNotificationReadUseCase;
import com.aibert.dosw.domain.exceptions.InvalidNotificationException;
import com.aibert.dosw.domain.exceptions.NotificationNotFoundException;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarkNotificationReadUseCase")
class MarkNotificationReadUseCaseTest {

    @Mock
    private NotificationRepositoryPort repository;

    @Mock
    private NotificationMapper mapper;

    @InjectMocks
    private MarkNotificationReadUseCase useCase;

    private static final UUID USER_ID_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    @DisplayName("markAsRead debe marcar la notificación correctamente")
    void shouldMarkNotificationAsRead() {
        Long notificationId = 1L;

        Notification notification = Notification.builder()
                .id(notificationId)
                .userId(USER_ID_1)
                .type(NotificationType.STUDY_SUGGESTION)
                .title("Estudia hoy")
                .message("Tienes pendiente Cálculo")
                .severity(NotificationSeverity.INFO)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(mapper.toResponse(any())).thenReturn(NotificationResponse.builder()
                .id(notificationId).read(true).build());

        NotificationResponse response = useCase.markAsRead(notificationId, USER_ID_1);

        assertThat(response).isNotNull();
        assertThat(response.isRead()).isTrue();
        verify(repository).markAsRead(eq(notificationId), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("markAsRead debe lanzar NotificationNotFoundException si no existe")
    void shouldThrowWhenNotificationNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.markAsRead(99L, USER_ID_1))
                .isInstanceOf(NotificationNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("markAsRead debe lanzar InvalidNotificationException si el userId no coincide")
    void shouldThrowWhenUserDoesNotOwnNotification() {
        Notification notification = Notification.builder()
                .id(1L)
                .userId(USER_ID_2)
                .type(NotificationType.OVERLOAD_ALERT)
                .title("Alerta")
                .severity(NotificationSeverity.HIGH)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> useCase.markAsRead(1L, USER_ID_1))
                .isInstanceOf(InvalidNotificationException.class);
    }
}
