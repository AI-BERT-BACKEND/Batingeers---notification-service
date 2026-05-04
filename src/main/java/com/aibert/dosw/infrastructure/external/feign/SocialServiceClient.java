package com.aibert.dosw.infrastructure.external.feign;

import com.aibert.dosw.infrastructure.external.dto.StudyInviteDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "social-service", url = "${services.social-service.url}")
public interface SocialServiceClient {

    @GetMapping("/api/v1/social/user/{userId}/study-invites/pending")
    List<StudyInviteDto> getPendingStudyInvites(@PathVariable("userId") Long userId);
}
