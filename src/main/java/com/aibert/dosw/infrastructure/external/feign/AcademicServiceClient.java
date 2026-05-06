package com.aibert.dosw.infrastructure.external.feign;

import com.aibert.dosw.infrastructure.external.dto.PerformanceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "academic-service", url = "${services.academic-service.url}")
public interface AcademicServiceClient {

    @GetMapping("/api/v1/academic/user/{userId}/performance")
    PerformanceDto getUserPerformance(@PathVariable("userId") Long userId);
}
