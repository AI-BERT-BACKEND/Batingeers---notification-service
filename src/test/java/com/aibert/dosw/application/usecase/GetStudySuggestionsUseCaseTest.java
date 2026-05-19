package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.response.NotificationResponse;
import com.aibert.dosw.application.mapper.NotificationMapper;
import com.aibert.dosw.application.usecase.notification.GetStudySuggestionsUseCase;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetStudySuggestionsUseCase")
class GetStudySuggestionsUseCaseTest {

    @Mock private NotificationRepositoryPort repository;
    @Mock private NotificationMapper mapper;

    @InjectMocks
    private GetStudySuggestionsUseCase useCase;

    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    @DisplayName("returns today's stored STUDY_SUGGESTION notifications")
    void returnsStoredSuggestions() {
        Notification suggestion = Notification.builder()
                .id(1L).userId(userId)
                .type(NotificationType.STUDY_SUGGESTION)
                .title("Algebra Workshop")
                .message("Priority 0.83: weight=3 (×0.6) + urgency 1/0.5 days (×0.4).")
                .severity(NotificationSeverity.INFO)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        NotificationResponse response = NotificationResponse.builder()
                .id(1L).type(NotificationType.STUDY_SUGGESTION).build();

        when(repository.findByUserIdAndTypeAndCreatedAtAfter(
                eq(userId), eq(NotificationType.STUDY_SUGGESTION), any()))
                .thenReturn(List.of(suggestion));
        when(mapper.toResponseList(List.of(suggestion))).thenReturn(List.of(response));

        List<NotificationResponse> result = useCase.getTodaySuggestions(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(NotificationType.STUDY_SUGGESTION);
    }

    @Test
    @DisplayName("returns empty list when no suggestions exist for today")
    void returnsEmptyWhenNoSuggestionsToday() {
        when(repository.findByUserIdAndTypeAndCreatedAtAfter(
                eq(userId), eq(NotificationType.STUDY_SUGGESTION), any()))
                .thenReturn(Collections.emptyList());
        when(mapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<NotificationResponse> result = useCase.getTodaySuggestions(userId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("returns multiple suggestions if planning-service sent more than one today")
    void returnsMultipleSuggestionsWhenPresent() {
        Notification s1 = Notification.builder().id(1L).userId(userId)
                .type(NotificationType.STUDY_SUGGESTION).severity(NotificationSeverity.INFO)
                .createdAt(LocalDateTime.now().minusHours(5)).build();
        Notification s2 = Notification.builder().id(2L).userId(userId)
                .type(NotificationType.STUDY_SUGGESTION).severity(NotificationSeverity.INFO)
                .createdAt(LocalDateTime.now().minusHours(1)).build();

        when(repository.findByUserIdAndTypeAndCreatedAtAfter(
                eq(userId), eq(NotificationType.STUDY_SUGGESTION), any()))
                .thenReturn(List.of(s1, s2));
        when(mapper.toResponseList(any())).thenReturn(List.of(
                NotificationResponse.builder().id(1L).type(NotificationType.STUDY_SUGGESTION).build(),
                NotificationResponse.builder().id(2L).type(NotificationType.STUDY_SUGGESTION).build()));

        List<NotificationResponse> result = useCase.getTodaySuggestions(userId);

        assertThat(result).hasSize(2);
    }
}
