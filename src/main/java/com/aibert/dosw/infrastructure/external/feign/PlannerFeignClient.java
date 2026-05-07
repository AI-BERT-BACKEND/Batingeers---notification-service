package com.aibert.dosw.infrastructure.external.feign;

import com.aibert.dosw.infrastructure.external.dto.PendingTaskDto;
import com.aibert.dosw.infrastructure.external.dto.WeeklyAvailabilityDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "planning-service", contextId = "plannerFeignClient",
        url = "${services.planning-service.url}")
public interface PlannerFeignClient {

    @GetMapping("/api/v1/planning/user/{userId}/weekly-availability")
    WeeklyAvailabilityDto getWeeklyAvailability(@PathVariable("userId") Long userId);

    @GetMapping("/api/v1/planning/user/{userId}/pending-tasks")
    List<PendingTaskDto> getPendingTasks(@PathVariable("userId") Long userId);
}
