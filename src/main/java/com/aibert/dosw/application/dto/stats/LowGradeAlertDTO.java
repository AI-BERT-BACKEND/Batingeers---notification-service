package com.aibert.dosw.application.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowGradeAlertDTO {
    private boolean active;
    private String bannerVariant;
    private String title;
    private String message;
    private List<String> subjectsAtRisk;
    private double currentAverage;
    private double threshold;
}
