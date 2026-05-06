package com.aibert.dosw.application.usecase.notification;

import com.aibert.dosw.application.dto.external.TodayPlanData;
import com.aibert.dosw.application.dto.response.NotificationResponse;
import com.aibert.dosw.application.mapper.NotificationMapper;
import com.aibert.dosw.domain.model.notification.Notification;
import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.GetStudySuggestionsPort;
import com.aibert.dosw.domain.ports.out.NotificationRepositoryPort;
import com.aibert.dosw.domain.ports.out.PlanningServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetStudySuggestionsUseCase implements GetStudySuggestionsPort {

    private final NotificationRepositoryPort repository;
    private final PlanningServicePort planningService;
    private final NotificationMapper mapper;

    @Override
    @Transactional
    public List<NotificationResponse> getTodaySuggestions(Long userId) {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIDNIGHT);
        List<Notification> existing = new ArrayList<>(repository.findByUserIdAndTypeAndCreatedAtAfter(
                userId, NotificationType.STUDY_SUGGESTION, startOfDay));

        if (existing.isEmpty()) {
            generateSuggestionForToday(userId).ifPresent(existing::add);
        }

        return mapper.toResponseList(existing);
    }

    private Optional<Notification> generateSuggestionForToday(Long userId) {
        try {
            return planningService.getTodayPlan(userId)
                    .filter(plan -> plan.getSuggestedTaskTitles() != null
                            && !plan.getSuggestedTaskTitles().isEmpty())
                    .map(plan -> {
                        Notification suggestion = buildSuggestionNotification(userId, plan);
                        return repository.save(suggestion);
                    });
        } catch (Exception ex) {
            log.warn("No se pudo obtener el plan de hoy para userId={}: {}", userId, ex.getMessage());
            return Optional.empty();
        }
    }

    private Notification buildSuggestionNotification(Long userId, TodayPlanData plan) {
        String taskList = String.join(", ", plan.getSuggestedTaskTitles());
        String message = plan.getFocusMessage() != null && !plan.getFocusMessage().isBlank()
                ? plan.getFocusMessage()
                : "Para hoy se sugiere trabajar en: " + taskList
                  + ". Tiempo estimado: " + plan.getTotalEstimatedMinutes() + " min.";

        return Notification.builder()
                .userId(userId)
                .type(NotificationType.STUDY_SUGGESTION)
                .title("¿Qué estudiar hoy?")
                .message(message)
                .severity(NotificationSeverity.INFO)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
