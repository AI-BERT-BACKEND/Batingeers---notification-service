package com.aibert.dosw.application.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayPlanData {
    private Long userId;
    private List<String> suggestedTaskTitles;
    private int totalEstimatedMinutes;
    private boolean overloaded;
    private String focusMessage;
}
