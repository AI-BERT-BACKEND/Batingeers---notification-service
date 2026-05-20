package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.infrastructure.external.dto.ProfileDto;

import java.util.Optional;
import java.util.UUID;

public interface ProfileServicePort {
    Optional<ProfileDto> getUserProfile(UUID userId);
}
