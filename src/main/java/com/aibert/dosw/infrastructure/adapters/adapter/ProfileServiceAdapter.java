package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.domain.ports.out.ProfileServicePort;
import com.aibert.dosw.infrastructure.external.dto.ProfileDto;
import com.aibert.dosw.infrastructure.external.feign.ProfileServiceClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileServiceAdapter implements ProfileServicePort {

    private final ProfileServiceClient profileServiceClient;

    @Override
    public Optional<ProfileDto> getUserProfile(Long userId) {
        try {
            return Optional.ofNullable(profileServiceClient.getUserProfile(userId));
        } catch (FeignException e) {
            log.warn("profile-service unavailable for userId={}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }
}
