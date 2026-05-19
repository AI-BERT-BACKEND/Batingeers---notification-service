package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.infrastructure.external.dto.ProfileDto;

import java.util.Optional;

public interface ProfileServicePort {
    Optional<ProfileDto> getUserProfile(Long userId);
}
