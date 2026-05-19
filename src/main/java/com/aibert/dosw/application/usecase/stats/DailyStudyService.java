package com.aibert.dosw.application.usecase.stats;

import com.aibert.dosw.application.dto.stats.StudySuggestionDTO;
import com.aibert.dosw.domain.model.notification.Notification;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.GetDailySuggestionPort;
import com.aibert.dosw.domain.ports.out.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyStudyService implements GetDailySuggestionPort {

    private final NotificationRepositoryPort notificationRepository;

    @Override
    public Optional<StudySuggestionDTO> getSuggestion(UUID userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        List<Notification> suggestions = notificationRepository
                .findByUserIdAndTypeAndCreatedAtAfter(userId, NotificationType.STUDY_SUGGESTION, startOfDay);

        return suggestions.stream()
                .max(Comparator.comparing(Notification::getCreatedAt))
                .map(n -> StudySuggestionDTO.builder()
                        .suggestedTask(n.getTitle())
                        .suggestionReason(n.getMessage())
                        .build());
    }
}
