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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileServiceAdapter")
class ProfileServiceAdapterTest {

    @Mock private ProfileServiceClient profileServiceClient;

    @InjectMocks
    private ProfileServiceAdapter adapter;

    private static final UUID USER_ID_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID USER_ID_3 = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Test
    @DisplayName("successful Feign call → returns Optional with ProfileDto")
    void successfulCall_returnsProfile() {
        ProfileDto dto = new ProfileDto(USER_ID_1, "jdoe", "jdoe@example.com", "John Doe", "STUDENT");
        when(profileServiceClient.getUserProfile(USER_ID_1)).thenReturn(dto);

        Optional<ProfileDto> result = adapter.getUserProfile(USER_ID_1);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(USER_ID_1);
        assertThat(result.get().getUsername()).isEqualTo("jdoe");
    }

    @Test
    @DisplayName("profile-service returns null → returns empty Optional")
    void nullResponse_returnsEmpty() {
        when(profileServiceClient.getUserProfile(USER_ID_2)).thenReturn(null);

        Optional<ProfileDto> result = adapter.getUserProfile(USER_ID_2);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("FeignException (service unavailable) → returns empty Optional")
    void feignException_returnsEmpty() {
        when(profileServiceClient.getUserProfile(USER_ID_3))
                .thenThrow(mock(FeignException.class));

        Optional<ProfileDto> result = adapter.getUserProfile(USER_ID_3);

        assertThat(result).isEmpty();
    }
}
