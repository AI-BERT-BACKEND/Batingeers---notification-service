package com.aibert.dosw.application.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertsResponseDTO {
    private OverloadAlertDTO overloadAlert;
    private LowGradeAlertDTO lowGradeAlert;
}
