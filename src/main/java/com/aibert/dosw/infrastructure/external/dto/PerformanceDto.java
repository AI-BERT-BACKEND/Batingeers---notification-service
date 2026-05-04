package com.aibert.dosw.infrastructure.external.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceDto {
    private Long userId;
    private List<SubjectPerformanceDto> subjects;
    private Double overallAverage;
    private boolean atRisk;
}
