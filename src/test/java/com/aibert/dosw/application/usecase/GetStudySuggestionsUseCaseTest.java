package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.external.TodayPlanData;
import com.aibert.dosw.application.dto.response.NotificationResponse;
import com.aibert.dosw.application.mapper.NotificationMapper;
import com.aibert.dosw.application.usecase.notification.GetStudySuggestionsUseCase;
import com.aibert.dosw.domain.model.notification.Notification;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.out.NotificationRepositoryPort;
import com.aibert.dosw.domain.ports.out.PlanningServicePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetStudySuggestionsUseCase")
class GetStudySuggestionsUseCaseTest {

    @Mock
    private NotificationRepositoryPort repository;

    @Mock
    private PlanningServicePort planningService;

    @Mock
    private NotificationMapper mapper;

    @InjectMocks
    private GetStudySuggestionsUseCase useCase;

    private final Long userId = 1L;

    @Test
    @DisplayName("debe retornar sugerencias existentes sin llamar al planning-service")
    void shouldReturnExistingSuggestionsWithoutCallingPlanning() {
        Notification existing = Notification.builder()
                .id(1L)
                .userId(userId)
                .type(NotificationType.STUDY_SUGGESTION)
                .title("¿Qué estudiar hoy?")
                .message("Para hoy: Cálculo, Física")
                .severity(NotificationSeverity.INFO)
                .createdAt(LocalDateTime.now())
                .build();

        NotificationResponse response = NotificationResponse.builder()
                .id(1L).type(NotificationType.STUDY_SUGGESTION).build();

        when(repository.findByUserIdAndTypeAndCreatedAtAfter(
                eq(userId), eq(NotificationType.STUDY_SUGGESTION), any()))
                .thenReturn(List.of(existing));
        when(mapper.toResponseList(any())).thenReturn(List.of(response));

        List<NotificationResponse> result = useCase.getTodaySuggestions(userId);

        assertThat(result).hasSize(1);
        verify(planningService, never()).getTodayPlan(any());
    }

    @Test
    @DisplayName("debe generar sugerencia consultando planning-service cuando no hay ninguna hoy")
    void shouldGenerateSuggestionFromPlanningWhenNoneExist() {
        TodayPlanData plan = TodayPlanData.builder()
                .userId(userId)
                .suggestedTaskTitles(List.of("Tarea Cálculo 1", "Leer capítulo Física"))
                .totalEstimatedMinutes(120)
                .overloaded(false)
                .build();

        Notification saved = Notification.builder()
                .id(10L)
                .userId(userId)
                .type(NotificationType.STUDY_SUGGESTION)
                .title("¿Qué estudiar hoy?")
                .severity(NotificationSeverity.INFO)
                .createdAt(LocalDateTime.now())
                .build();

        NotificationResponse response = NotificationResponse.builder()
                .id(10L).type(NotificationType.STUDY_SUGGESTION).build();

        when(repository.findByUserIdAndTypeAndCreatedAtAfter(
                eq(userId), eq(NotificationType.STUDY_SUGGESTION), any()))
                .thenReturn(Collections.emptyList());
        when(planningService.getTodayPlan(userId)).thenReturn(Optional.of(plan));
        when(repository.save(any())).thenReturn(saved);
        when(mapper.toResponseList(any())).thenReturn(List.of(response));

        List<NotificationResponse> result = useCase.getTodaySuggestions(userId);

        assertThat(result).hasSize(1);
        verify(repository).save(any());
    }

    @Test
    @DisplayName("debe retornar lista vacía si planning-service falla")
    void shouldReturnEmptyListWhenPlanningServiceFails() {
        when(repository.findByUserIdAndTypeAndCreatedAtAfter(
                eq(userId), eq(NotificationType.STUDY_SUGGESTION), any()))
                .thenReturn(Collections.emptyList());
        when(planningService.getTodayPlan(userId))
                .thenThrow(new RuntimeException("Service unavailable"));
        when(mapper.toResponseList(any())).thenReturn(Collections.emptyList());

        List<NotificationResponse> result = useCase.getTodaySuggestions(userId);

        assertThat(result).isEmpty();
    }
}
