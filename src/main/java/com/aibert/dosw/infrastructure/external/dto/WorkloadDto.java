package com.aibert.dosw.infrastructure.external.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadDto {
    private Long userId;
    private int totalTasks;
    private int urgentTasks;
    private int overdueTasks;
    private String workloadLevel;
}
