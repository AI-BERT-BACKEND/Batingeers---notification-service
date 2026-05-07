package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.application.dto.external.PendingTaskData;

import java.util.List;

public interface PendingTasksPort {
    List<PendingTaskData> getPendingTasks(Long userId);
}
