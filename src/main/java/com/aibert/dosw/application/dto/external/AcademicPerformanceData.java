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
public class AcademicPerformanceData {
    private Long userId;
    private List<String> atRiskSubjectNames;
    private double overallAverage;
    private boolean atRisk;
}
