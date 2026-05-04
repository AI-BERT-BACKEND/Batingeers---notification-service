package com.aibert.dosw.infrastructure.external.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailyTaskDto {
    private Long taskId;
    private String title;
    private String subjectName;
    private int estimatedMinutes;
    private String priority;
}
