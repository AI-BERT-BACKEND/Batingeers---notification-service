package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.application.dto.external.TaskWorkloadData;

import java.util.Optional;

public interface TaskServicePort {
    Optional<TaskWorkloadData> getUserWorkload(Long userId);
}
