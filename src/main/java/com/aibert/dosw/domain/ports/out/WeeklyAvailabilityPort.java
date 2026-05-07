package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.application.dto.external.WeeklyAvailabilityData;

import java.util.Optional;

public interface WeeklyAvailabilityPort {
    Optional<WeeklyAvailabilityData> getWeeklyAvailability(Long userId);
}
