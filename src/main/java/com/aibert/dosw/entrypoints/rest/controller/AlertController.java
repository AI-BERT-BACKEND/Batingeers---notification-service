package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.stats.AlertsResponseDTO;
import com.aibert.dosw.config.UserPrincipal;
import com.aibert.dosw.domain.ports.in.GetStatsAlertsPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Overload and low academic performance alerts (R22)")
public class AlertController {

    private final GetStatsAlertsPort getStatsAlertsPort;

    @GetMapping("/alerts")
    @Operation(
        summary = "R22 — Overload and low performance alerts",
        description = "Evaluates weekly workload vs. availability and projected grades vs. threshold 3.0. " +
                      "Returns OverloadAlertDTO and LowGradeAlertDTO banners with warning or critical variant."
    )
    public ResponseEntity<AlertsResponseDTO> getAlerts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(getStatsAlertsPort.getAlerts(principal.getUserId()));
    }
}
