package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.stats.StudySuggestionDTO;
import com.aibert.dosw.config.UserPrincipal;
import com.aibert.dosw.domain.ports.in.GetDailySuggestionPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
public class SuggestionController {

    private final GetDailySuggestionPort getDailySuggestionPort;

    @GetMapping("/suggestion")
    @Operation(
        summary = "R23 — Get the highest-priority study suggestion",
        description = "Fetches pending tasks from planning-service and applies the priority formula " +
                      "`weight×0.6 + (1/days)×0.4` to each task, where `weight` is the task priority " +
                      "integer and `days` is the number of days until the due date (minimum 0.5 to avoid " +
                      "division by zero). Returns the single task with the highest computed score as a " +
                      "`StudySuggestionDTO`, including the suggested task title, subject name, due date, " +
                      "priority score, and a human-readable explanation of the calculation. " +
                      "Returns **204 No Content** if the student has no pending tasks."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Study suggestion computed and returned"),
        @ApiResponse(responseCode = "204", description = "No pending tasks found — no suggestion to return",
                     content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                     content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "503", description = "planning-service is unavailable",
                     content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<StudySuggestionDTO> getSuggestion(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return getDailySuggestionPort.getSuggestion(principal.getUserId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
