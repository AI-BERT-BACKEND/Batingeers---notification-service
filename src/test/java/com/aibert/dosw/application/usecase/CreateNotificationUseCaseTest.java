package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.request.CreateNotificationRequest;
import com.aibert.dosw.application.dto.response.NotificationResponse;
import com.aibert.dosw.application.mapper.NotificationMapper;
import com.aibert.dosw.application.usecase.notification.CreateNotificationUseCase;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateNotificationUseCase")
class CreateNotificationUseCaseTest {

    @Mock
    private NotificationRepositoryPort repository;

    @Mock
    private NotificationMapper mapper;

    @InjectMocks
    private CreateNotificationUseCase useCase;

    @Test
    @DisplayName("debe crear y retornar la notificación correctamente")
    void shouldCreateAndReturnNotification() {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(1L)
                .type(NotificationType.OVERLOAD_ALERT)
                .title("Sobrecarga")
                .message("Tienes demasiadas tareas")
                .severity(NotificationSeverity.HIGH)
                .build();

        Notification mapped = Notification.builder()
                .userId(1L)
                .type(NotificationType.OVERLOAD_ALERT)
                .title("Sobrecarga")
                .message("Tienes demasiadas tareas")
                .severity(NotificationSeverity.HIGH)
                .build();

        Notification saved = Notification.builder()
                .id(1L)
                .userId(1L)
                .type(NotificationType.OVERLOAD_ALERT)
                .title("Sobrecarga")
                .message("Tienes demasiadas tareas")
                .severity(NotificationSeverity.HIGH)
                .build();

        NotificationResponse expected = NotificationResponse.builder()
                .id(1L)
                .userId(1L)
                .type(NotificationType.OVERLOAD_ALERT)
                .title("Sobrecarga")
                .build();

        when(mapper.toDomain(request)).thenReturn(mapped);
        when(repository.save(any())).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(expected);

        NotificationResponse result = useCase.create(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo(NotificationType.OVERLOAD_ALERT);
        verify(repository).save(any());
    }
}
