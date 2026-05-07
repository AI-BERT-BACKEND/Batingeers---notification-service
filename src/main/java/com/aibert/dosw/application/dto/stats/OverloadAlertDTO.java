package com.aibert.dosw.application.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverloadAlertDTO {
    private boolean active;
    private String bannerVariant;
    private String title;
    private String message;
    private int weeklyTaskCount;
    private int availableHours;
}
