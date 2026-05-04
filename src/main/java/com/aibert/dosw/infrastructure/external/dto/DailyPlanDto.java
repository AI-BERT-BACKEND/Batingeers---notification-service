package com.aibert.dosw.infrastructure.external.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailyPlanDto {
    private Long userId;
    private List<DailyTaskDto> suggestedTasks;
    private int totalEstimatedMinutes;
    private boolean overloaded;
    private String focusMessage;
}
