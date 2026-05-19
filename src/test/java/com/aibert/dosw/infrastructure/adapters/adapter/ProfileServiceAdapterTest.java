package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.infrastructure.external.dto.ProfileDto;
import com.aibert.dosw.infrastructure.external.feign.ProfileServiceClient;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileServiceAdapter")
class ProfileServiceAdapterTest {

    @Mock private ProfileServiceClient profileServiceClient;

    @InjectMocks
    private ProfileServiceAdapter adapter;

    @Test
    @DisplayName("successful Feign call → returns Optional with ProfileDto")
    void successfulCall_returnsProfile() {
        ProfileDto dto = new ProfileDto(1L, "jdoe", "jdoe@example.com", "John Doe", "STUDENT");
        when(profileServiceClient.getUserProfile(1L)).thenReturn(dto);

        Optional<ProfileDto> result = adapter.getUserProfile(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(1L);
        assertThat(result.get().getUsername()).isEqualTo("jdoe");
    }

    @Test
    @DisplayName("profile-service returns null → returns empty Optional")
    void nullResponse_returnsEmpty() {
        when(profileServiceClient.getUserProfile(2L)).thenReturn(null);

        Optional<ProfileDto> result = adapter.getUserProfile(2L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("FeignException (service unavailable) → returns empty Optional")
    void feignException_returnsEmpty() {
        when(profileServiceClient.getUserProfile(3L))
                .thenThrow(mock(FeignException.class));

        Optional<ProfileDto> result = adapter.getUserProfile(3L);

        assertThat(result).isEmpty();
    }
}
