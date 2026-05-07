package com.aibert.dosw.application.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyAvailabilityData {
    private Long userId;
    private int totalAvailableHours;
    private int configuredDays;

    public boolean isConfigured() {
        return configuredDays > 0;
    }
}
