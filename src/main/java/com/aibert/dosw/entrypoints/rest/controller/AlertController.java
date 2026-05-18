package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.stats.AlertsResponseDTO;
import com.aibert.dosw.config.UserPrincipal;
import com.aibert.dosw.domain.ports.in.GetStatsAlertsPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@Tag(name = "Stats — Alerts & Suggestions", description = "Real-time academic alerts and daily study suggestion (R22, R23)")
public class AlertController {

    private final GetStatsAlertsPort getStatsAlertsPort;

    @GetMapping("/alerts")
    @Operation(
        summary = "R22 — Get overload and low-grade alerts",
        description = """
            Evaluates two independent alert conditions for the authenticated user and returns \
            both results in a single response object.

            **Overload alert** (`overloadAlert`): compares estimated required hours \
            (totalPendingTasks × 2 h) against the student's configured weekly availability from \
            planning-service. Returns `active=false` with title 'Weekly availability not configured' \
            if no availability is set up. When active, includes `bannerVariant` (`warning` or `critical`), \
            `requiredHours`, `availableHours`, `overloadHours`, and a `suggestedAction`.

            **Low-grade alert** (`lowGradeAlert`): fetches projected grades from academic-service and \
            compares them against the minimum threshold of 3.0. Returns per-subject risk objects \
            (`riskLevel`: Medium / High / Critical) sorted ascending by projected grade, plus an \
            `alertTitle`, `alertMessage`, `recommendation`, and `generatedDate`.

            Both alerts are always present in the response — check the `active` field to determine \
            whether each alert should be displayed.
            """
    )
    @ApiResponse(responseCode = "200", description = "Alerts evaluated and returned successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                 content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "503", description = "One or more external microservices are unavailable",
                 content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<AlertsResponseDTO> getAlerts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(getStatsAlertsPort.getAlerts(principal.getUserId()));
    }
}
