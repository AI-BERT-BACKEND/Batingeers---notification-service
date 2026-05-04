package com.aibert.dosw.application.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskWorkloadData {
    private Long userId;
    private int totalTasks;
    private int urgentTasks;
    private int overdueTasks;
    private String workloadLevel;

    public boolean isCritical() {
        return "CRITICAL".equalsIgnoreCase(workloadLevel);
    }

    public boolean isHigh() {
        return "HIGH".equalsIgnoreCase(workloadLevel) || isCritical();
    }
}
