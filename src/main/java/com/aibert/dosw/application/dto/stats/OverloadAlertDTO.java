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
@Schema(description = "Overload alert based on pending task hours vs. weekly availability")
public class OverloadAlertDTO {

    @Schema(description = "Whether the alert is active", example = "true")
    private boolean active;

    @Schema(description = "Banner display variant", example = "critical", allowableValues = {"warning", "critical"})
    private String bannerVariant;

    @Schema(description = "Alert title", example = "Posible sobrecarga")
    private String title;

    @Schema(description = "Descriptive alert message", example = "Tienes 8 tareas (16.0 h estimadas) vs 10.0 h disponibles esta semana.")
    private String message;

    @Schema(description = "Suggested action to reduce workload", example = "Prioriza las tareas urgentes y reprograma las de menor prioridad.")
    private String suggestedAction;

    @Schema(description = "Total estimated hours required to complete all pending tasks", example = "16.0")
    private double requiredHours;

    @Schema(description = "Total hours available in the student's weekly schedule", example = "10.0")
    private double availableHours;

    @Schema(description = "Difference between required and available hours (requiredHours - availableHours)", example = "6.0")
    private double overloadHours;
}
