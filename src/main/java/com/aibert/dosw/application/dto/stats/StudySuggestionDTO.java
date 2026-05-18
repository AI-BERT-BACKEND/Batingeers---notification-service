package com.aibert.dosw.application.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Daily study suggestion computed by the priority formula: weight×0.6 + (1/days)×0.4")
public class StudySuggestionDTO {

    @Schema(description = "Title of the task with the highest calculated priority", example = "Algebra Workshop")
    private String suggestedTask;

    @Schema(description = "Human-readable explanation of how the priority score was calculated",
            example = "Priority 0.83: weight=1 (×0.6) + urgency 1/0.5 days (×0.4).")
    private String suggestionReason;

    @Schema(description = "Name of the subject the suggested task belongs to", example = "Linear Algebra")
    private String subjectName;

    @Schema(description = "Computed priority score used to select this task over others", example = "0.83")
    private double priority;

    @Schema(description = "Due date of the suggested task", example = "2026-05-20")
    private LocalDate dueDate;
}
