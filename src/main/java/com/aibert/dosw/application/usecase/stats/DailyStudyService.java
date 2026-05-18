package com.aibert.dosw.application.usecase.stats;

import com.aibert.dosw.application.dto.external.PendingTaskData;
import com.aibert.dosw.application.dto.stats.StudySuggestionDTO;
import com.aibert.dosw.domain.ports.in.GetDailySuggestionPort;
import com.aibert.dosw.domain.ports.out.PendingTasksPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyStudyService implements GetDailySuggestionPort {

    private static final double WEIGHT_FACTOR  = 0.6;
    private static final double URGENCY_FACTOR = 0.4;
    static final double MIN_DAYS_DIVISOR = 0.5;

    private final PendingTasksPort pendingTasksPort;

    @Override
    public Optional<StudySuggestionDTO> getSuggestion(Long userId) {
        List<PendingTaskData> tasks = pendingTasksPort.getPendingTasks(userId);
        if (tasks.isEmpty()) {
            return Optional.empty();
        }
        return tasks.stream()
                .max(Comparator.comparingDouble(this::calculatePriority))
                .map(this::toSuggestionDTO);
    }

    public double calculatePriority(PendingTaskData task) {
        double weight = task.getPriorityWeight();
        double days = Math.max(task.getDaysUntilDue(), MIN_DAYS_DIVISOR);
        return weight * WEIGHT_FACTOR + (1.0 / days) * URGENCY_FACTOR;
    }

    private StudySuggestionDTO toSuggestionDTO(PendingTaskData task) {
        double priority = calculatePriority(task);
        double days = Math.max(task.getDaysUntilDue(), MIN_DAYS_DIVISOR);
        String reason = String.format(
                "Priority %.2f: weight=%d (×0.6) + urgency 1/%.1f days (×0.4).",
                priority, task.getPriorityWeight(), days);

        return StudySuggestionDTO.builder()
                .suggestedTask(task.getTitle())
                .suggestionReason(reason)
                .subjectName(task.getSubjectName())
                .priority(priority)
                .dueDate(task.getDueDate())
                .build();
    }
}
