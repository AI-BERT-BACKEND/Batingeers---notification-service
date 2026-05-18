package com.aibert.dosw.application.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Low academic performance alert based on projected subject grades")
public class LowGradeAlertDTO {

    @Schema(description = "Whether the alert is active", example = "true")
    private boolean active;

    @Schema(description = "Banner display variant", example = "warning", allowableValues = {"warning", "critical"})
    private String bannerVariant;

    @Schema(description = "Alert title (legacy field)", example = "Bajo rendimiento académico")
    private String title;

    @Schema(description = "Alert title as defined in AIB-34 spec", example = "Materias en riesgo académico")
    private String alertTitle;

    @Schema(description = "Descriptive alert message with current average", example = "Tu promedio actual es 2.8 (umbral mínimo: 3.0).")
    private String message;

    @Schema(description = "Actionable alert message listing subjects and projected grades")
    private String alertMessage;

    @Schema(description = "General recommendation for the student")
    private String recommendation;

    @Schema(description = "Student's current overall academic average", example = "2.8")
    private double currentAverage;

    @Schema(description = "Minimum passing grade threshold", example = "3.0")
    private double threshold;

    @Schema(description = "Names of subjects currently at academic risk")
    private List<String> subjectsAtRisk;

    @Schema(description = "Detailed risk information per subject, sorted ascending by projected grade")
    private List<RiskSubjectDTO> riskSubjects;

    @Schema(description = "Timestamp when this alert was generated")
    private LocalDateTime generatedDate;
}
