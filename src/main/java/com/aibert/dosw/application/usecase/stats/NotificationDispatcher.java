package com.aibert.dosw.application.usecase.stats;

import com.aibert.dosw.application.dto.stats.AlertsResponseDTO;
import com.aibert.dosw.domain.ports.in.GetStatsAlertsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationDispatcher implements GetStatsAlertsPort {

    private final AlertService alertService;

    @Override
    public AlertsResponseDTO getAlerts(Long userId) {
        return AlertsResponseDTO.builder()
                .overloadAlert(alertService.evaluateOverloadAlert(userId))
                .lowGradeAlert(alertService.evaluateLowGradeAlert(userId))
                .build();
    }
}
