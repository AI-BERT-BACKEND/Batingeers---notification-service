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
@Tag(name = "Estadísticas", description = "Alertas de sobrecarga y bajo rendimiento (R22)")
public class AlertController {

    private final GetStatsAlertsPort getStatsAlertsPort;

    @GetMapping("/alerts")
    @Operation(
        summary = "R22 — Alertas de sobrecarga y bajo rendimiento",
        description = "Evalúa la carga semanal vs. disponibilidad y notas proyectadas vs. umbral 3.0. " +
                      "Retorna banners OverloadAlertDTO y LowGradeAlertDTO con variant warning o critical."
    )
    public ResponseEntity<AlertsResponseDTO> getAlerts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(getStatsAlertsPort.getAlerts(principal.getUserId()));
    }
}
