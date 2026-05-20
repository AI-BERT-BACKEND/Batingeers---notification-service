package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.stats.StudySuggestionDTO;

import java.util.Optional;
import java.util.UUID;

public interface GetDailySuggestionPort {
    Optional<StudySuggestionDTO> getSuggestion(UUID userId);
}
