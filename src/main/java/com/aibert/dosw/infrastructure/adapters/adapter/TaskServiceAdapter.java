package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.application.dto.external.TaskWorkloadData;
import com.aibert.dosw.domain.ports.out.TaskServicePort;
import com.aibert.dosw.infrastructure.external.dto.WorkloadDto;
import com.aibert.dosw.infrastructure.external.feign.TaskServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TaskServiceAdapter implements TaskServicePort {

    private final TaskServiceClient client;

    @Override
    public Optional<TaskWorkloadData> getUserWorkload(Long userId) {
        WorkloadDto dto = client.getUserWorkload(userId);
        if (dto == null) return Optional.empty();
        return Optional.of(TaskWorkloadData.builder()
                .userId(dto.getUserId())
                .totalTasks(dto.getTotalTasks())
                .urgentTasks(dto.getUrgentTasks())
                .overdueTasks(dto.getOverdueTasks())
                .workloadLevel(dto.getWorkloadLevel())
                .build());
    }
}
