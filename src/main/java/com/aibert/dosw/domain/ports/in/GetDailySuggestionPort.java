package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.stats.StudySuggestionDTO;

import java.util.Optional;

public interface GetDailySuggestionPort {
    Optional<StudySuggestionDTO> getSuggestion(Long userId);
}
