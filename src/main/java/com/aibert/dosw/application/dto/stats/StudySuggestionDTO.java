package com.aibert.dosw.application.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySuggestionDTO {
    private String suggestedTask;
    private String suggestionReason;
    private String subjectName;
    private double priority;
    private LocalDate dueDate;
}
