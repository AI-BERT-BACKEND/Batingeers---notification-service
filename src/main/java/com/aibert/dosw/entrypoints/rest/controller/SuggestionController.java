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
@Tag(name = "Estadísticas", description = "Sugerencia diaria de estudio (R23)")
public class SuggestionController {

    private final GetDailySuggestionPort getDailySuggestionPort;

    @GetMapping("/suggestion")
    @Operation(
        summary = "R23 — ¿Qué estudiar hoy?",
        description = "Aplica la fórmula peso×0.6 + 1/días×0.4 sobre las tareas pendientes " +
                      "y devuelve la tarea/materia con mayor prioridad calculada. " +
                      "Retorna 204 si no hay tareas pendientes."
    )
    public ResponseEntity<StudySuggestionDTO> getSuggestion(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return getDailySuggestionPort.getSuggestion(principal.getUserId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
