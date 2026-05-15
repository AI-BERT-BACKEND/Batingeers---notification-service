package com.aibert.dosw.infrastructure.external.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubjectPerformanceDto {
    private Long subjectId;
    private String subjectName;
    private Double currentGrade;
    private Double targetGrade;
    private Double projectedGrade;
    private boolean atRisk;
}
