package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.application.dto.external.TodayPlanData;
import com.aibert.dosw.domain.ports.out.PlanningServicePort;
import com.aibert.dosw.infrastructure.external.dto.DailyPlanDto;
import com.aibert.dosw.infrastructure.external.feign.PlanningServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PlanningServiceAdapter implements PlanningServicePort {

    private final PlanningServiceClient client;

    @Override
    public Optional<TodayPlanData> getTodayPlan(Long userId) {
        DailyPlanDto dto = client.getUserTodayPlan(userId);
        if (dto == null) return Optional.empty();

        List<String> taskTitles = dto.getSuggestedTasks() == null
                ? Collections.emptyList()
                : dto.getSuggestedTasks().stream()
                        .map(t -> t.getTitle())
                        .toList();

        return Optional.of(TodayPlanData.builder()
                .userId(dto.getUserId())
                .suggestedTaskTitles(taskTitles)
                .totalEstimatedMinutes(dto.getTotalEstimatedMinutes())
                .overloaded(dto.isOverloaded())
                .focusMessage(dto.getFocusMessage())
                .build());
    }
}
