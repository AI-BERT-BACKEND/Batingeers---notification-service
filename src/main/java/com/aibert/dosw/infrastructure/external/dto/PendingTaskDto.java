package com.aibert.dosw.infrastructure.external.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PendingTaskDto {
    private Long taskId;
    private String title;
    private String subjectName;
    private String priority;
    private LocalDate dueDate;
}
