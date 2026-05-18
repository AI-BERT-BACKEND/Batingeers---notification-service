package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.application.dto.external.PendingTaskData;
import com.aibert.dosw.application.dto.external.WeeklyAvailabilityData;
import com.aibert.dosw.domain.ports.out.PendingTasksPort;
import com.aibert.dosw.domain.ports.out.WeeklyAvailabilityPort;
import com.aibert.dosw.infrastructure.external.dto.PendingTaskDto;
import com.aibert.dosw.infrastructure.external.dto.WeeklyAvailabilityDto;
import com.aibert.dosw.infrastructure.external.feign.PlannerFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanningModuleAdapter implements WeeklyAvailabilityPort, PendingTasksPort {

    private final PlannerFeignClient client;

    @Override
    public Optional<WeeklyAvailabilityData> getWeeklyAvailability(Long userId) {
        try {
            WeeklyAvailabilityDto dto = client.getWeeklyAvailability(userId);
            if (dto == null) return Optional.empty();
            return Optional.of(WeeklyAvailabilityData.builder()
                    .userId(dto.getUserId())
                    .totalAvailableHours(dto.getTotalAvailableHours())
                    .configuredDays(dto.getConfiguredDays())
                    .build());
        } catch (Exception ex) {
            log.warn("Could not retrieve weekly availability for userId={}: {}", userId, ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<PendingTaskData> getPendingTasks(Long userId) {
        try {
            List<PendingTaskDto> dtos = client.getPendingTasks(userId);
            if (dtos == null) return Collections.emptyList();
            return dtos.stream()
                    .map(dto -> PendingTaskData.builder()
                            .taskId(dto.getTaskId())
                            .title(dto.getTitle())
                            .subjectName(dto.getSubjectName())
                            .priority(dto.getPriority())
                            .dueDate(dto.getDueDate())
                            .build())
                    .toList();
        } catch (Exception ex) {
            log.warn("Could not retrieve pending tasks for userId={}: {}", userId, ex.getMessage());
            return Collections.emptyList();
        }
    }
}
