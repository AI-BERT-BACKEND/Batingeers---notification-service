package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.stats.AlertsResponseDTO;

public interface GetStatsAlertsPort {
    AlertsResponseDTO getAlerts(Long userId);
}
