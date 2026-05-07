package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.application.dto.external.StudyInviteData;
import com.aibert.dosw.infrastructure.external.dto.StudyInviteDto;
import com.aibert.dosw.infrastructure.external.feign.SocialServiceClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SocialServiceAdapter")
class SocialServiceAdapterTest {

    @Mock private SocialServiceClient client;

    @InjectMocks
    private SocialServiceAdapter adapter;

    private final Long userId = 1L;

    @Test
    @DisplayName("getPendingStudyInvites: lista válida → mapea todos los campos")
    void getPendingStudyInvites_validList_mapsCorrectly() {
        StudyInviteDto dto = new StudyInviteDto(10L, 2L, "maria", "Cálculo I",
                "2026-05-10T14:00", "PENDING");
        when(client.getPendingStudyInvites(userId)).thenReturn(List.of(dto));

        List<StudyInviteData> result = adapter.getPendingStudyInvites(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInviteId()).isEqualTo(10L);
        assertThat(result.get(0).getFromUsername()).isEqualTo("maria");
        assertThat(result.get(0).getSubjectName()).isEqualTo("Cálculo I");
    }

    @Test
    @DisplayName("getPendingStudyInvites: lista vacía → retorna lista vacía")
    void getPendingStudyInvites_emptyList_returnsEmpty() {
        when(client.getPendingStudyInvites(userId)).thenReturn(Collections.emptyList());

        List<StudyInviteData> result = adapter.getPendingStudyInvites(userId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getPendingStudyInvites: respuesta null → retorna lista vacía sin NPE")
    void getPendingStudyInvites_nullResponse_returnsEmpty() {
        when(client.getPendingStudyInvites(userId)).thenReturn(null);

        List<StudyInviteData> result = adapter.getPendingStudyInvites(userId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getPendingStudyInvites: múltiples invites → todos mapeados")
    void getPendingStudyInvites_multipleInvites_allMapped() {
        List<StudyInviteDto> dtos = List.of(
                new StudyInviteDto(1L, 2L, "pedro", "Física", "2026-05-11T10:00", "PENDING"),
                new StudyInviteDto(2L, 3L, "ana",   "Inglés", "2026-05-12T16:00", "PENDING")
        );
        when(client.getPendingStudyInvites(userId)).thenReturn(dtos);

        List<StudyInviteData> result = adapter.getPendingStudyInvites(userId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(StudyInviteData::getFromUsername)
                .containsExactly("pedro", "ana");
    }
}
