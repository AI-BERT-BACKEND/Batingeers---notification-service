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
@Schema(description = "Combined alert response containing both the overload alert and the low-grade alert for a student")
public class AlertsResponseDTO {

    @Schema(description = "Overload alert evaluated from weekly task hours vs. available schedule hours")
    private OverloadAlertDTO overloadAlert;

    @Schema(description = "Low academic performance alert evaluated from projected subject grades")
    private LowGradeAlertDTO lowGradeAlert;
}
