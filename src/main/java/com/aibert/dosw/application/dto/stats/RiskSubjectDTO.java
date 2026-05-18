package com.aibert.dosw.application.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Subject at academic risk with projected grade and action recommendation")
public class RiskSubjectDTO {

    @Schema(description = "Subject identifier (from academic-service)", example = "101")
    private String subjectId;

    @Schema(description = "Subject name", example = "Cálculo I")
    private String name;

    @Schema(description = "Projected final grade for the subject (scale 0.0–5.0)", example = "2.1")
    private double projectedGrade;

    @Schema(description = "Academic risk level", example = "Alto", allowableValues = {"Medio", "Alto", "Crítico"})
    private String riskLevel;

    @Schema(description = "Brief actionable recommendation for this subject")
    private String recommendation;
}
