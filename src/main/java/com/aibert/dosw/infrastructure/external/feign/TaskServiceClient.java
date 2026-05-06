package com.aibert.dosw.infrastructure.external.feign;

import com.aibert.dosw.infrastructure.external.dto.WorkloadDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "task-service", url = "${services.task-service.url}")
public interface TaskServiceClient {

    @GetMapping("/api/v1/tasks/user/{userId}/workload")
    WorkloadDto getUserWorkload(@PathVariable("userId") Long userId);
}
