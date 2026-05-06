package com.aibert.dosw.infrastructure.external.feign;

import com.aibert.dosw.infrastructure.external.dto.DailyPlanDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "planning-service", url = "${services.planning-service.url}")
public interface PlanningServiceClient {

    @GetMapping("/api/v1/planning/user/{userId}/today")
    DailyPlanDto getUserTodayPlan(@PathVariable("userId") Long userId);
}
