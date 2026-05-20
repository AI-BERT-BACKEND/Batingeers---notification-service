package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.stats.AlertsResponseDTO;
import java.util.UUID;

public interface GetStatsAlertsPort {
    AlertsResponseDTO getAlerts(UUID userId);
}
