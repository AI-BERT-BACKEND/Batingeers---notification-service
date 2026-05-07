package com.aibert.dosw.infrastructure.external.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyAvailabilityDto {
    private Long userId;
    private int totalAvailableHours;
    private int configuredDays;
}
