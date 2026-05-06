package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.application.dto.external.TodayPlanData;

import java.util.Optional;

public interface PlanningServicePort {
    Optional<TodayPlanData> getTodayPlan(Long userId);
}
