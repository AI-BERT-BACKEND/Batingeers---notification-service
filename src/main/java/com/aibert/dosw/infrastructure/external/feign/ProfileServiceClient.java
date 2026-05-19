package com.aibert.dosw.infrastructure.external.feign;

import com.aibert.dosw.infrastructure.external.dto.ProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "profile-service", url = "${services.profile-service.url}")
public interface ProfileServiceClient {

    @GetMapping("/api/v1/profiles/{userId}")
    ProfileDto getUserProfile(@PathVariable("userId") Long userId);
}
