package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.stats.StudySuggestionDTO;
import com.aibert.dosw.application.usecase.stats.DailyStudyService;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DailyStudyService")
class DailyStudyServiceTest {

    @Mock private NotificationRepositoryPort notificationRepository;

    @InjectMocks
    private DailyStudyService dailyStudyService;

    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    @DisplayName("no STUDY_SUGGESTION today → returns empty Optional")
    void noSuggestionToday_returnsEmpty() {
        when(notificationRepository.findByUserIdAndTypeAndCreatedAtAfter(
                eq(userId), eq(NotificationType.STUDY_SUGGESTION), any()))
                .thenReturn(Collections.emptyList());

        Optional<StudySuggestionDTO> result = dailyStudyService.getSuggestion(userId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("STUDY_SUGGESTION exists today → returns DTO with title and reason")
    void suggestionExists_returnsDTOWithTitleAndReason() {
        Notification suggestion = buildSuggestion(
                "Algebra Workshop",
                "Priority 0.83: weight=3 (×0.6) + urgency 1/0.5 days (×0.4).",
                LocalDateTime.now().minusHours(1));

        when(notificationRepository.findByUserIdAndTypeAndCreatedAtAfter(
                eq(userId), eq(NotificationType.STUDY_SUGGESTION), any()))
                .thenReturn(List.of(suggestion));

        Optional<StudySuggestionDTO> result = dailyStudyService.getSuggestion(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getSuggestedTask()).isEqualTo("Algebra Workshop");
        assertThat(result.get().getSuggestionReason()).contains("Priority");
    }

    @Test
    @DisplayName("multiple suggestions today → returns the most recent one")
    void multipleSuggestions_returnsMostRecent() {
        Notification older = buildSuggestion("Calculus I", "Older suggestion",
                LocalDateTime.now().minusHours(5));
        Notification newer = buildSuggestion("Linear Algebra", "Newer suggestion",
                LocalDateTime.now().minusHours(1));

        when(notificationRepository.findByUserIdAndTypeAndCreatedAtAfter(
                eq(userId), eq(NotificationType.STUDY_SUGGESTION), any()))
                .thenReturn(List.of(older, newer));

        Optional<StudySuggestionDTO> result = dailyStudyService.getSuggestion(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getSuggestedTask()).isEqualTo("Linear Algebra");
    }

    private Notification buildSuggestion(String title, String message, LocalDateTime createdAt) {
        return Notification.builder()
                .userId(userId)
                .type(NotificationType.STUDY_SUGGESTION)
                .title(title)
                .message(message)
                .severity(NotificationSeverity.INFO)
                .createdAt(createdAt)
                .build();
    }
}
