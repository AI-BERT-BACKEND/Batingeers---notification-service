package com.aibert.dosw.application.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingTaskData {
    private Long taskId;
    private String title;
    private String subjectName;
    private String priority;
    private LocalDate dueDate;

    public int getPriorityWeight() {
        if (priority == null) return 1;
        return switch (priority.toUpperCase()) {
            case "HIGH"   -> 3;
            case "MEDIUM" -> 2;
            default       -> 1;
        };
    }

    public long getDaysUntilDue() {
        if (dueDate == null) return 7;
        long days = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        return Math.max(days, 0);
    }
}
