package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.stats.StudySuggestionDTO;
import com.aibert.dosw.config.UserPrincipal;
import com.aibert.dosw.domain.ports.in.GetDailySuggestionPort;
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
@Tag(name = "Statistics", description = "Daily study suggestion (R23)")
public class SuggestionController {

    private final GetDailySuggestionPort getDailySuggestionPort;

    @GetMapping("/suggestion")
    @Operation(
        summary = "R23 — What to study today?",
        description = "Applies the formula weight×0.6 + 1/days×0.4 over pending tasks " +
                      "and returns the task/subject with the highest calculated priority. " +
                      "Returns 204 if there are no pending tasks."
    )
    public ResponseEntity<StudySuggestionDTO> getSuggestion(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return getDailySuggestionPort.getSuggestion(principal.getUserId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
