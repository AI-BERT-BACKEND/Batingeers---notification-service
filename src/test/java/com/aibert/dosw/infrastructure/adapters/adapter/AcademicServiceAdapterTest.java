package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.application.dto.external.AcademicPerformanceData;
import com.aibert.dosw.infrastructure.external.dto.PerformanceDto;
import com.aibert.dosw.infrastructure.external.dto.SubjectPerformanceDto;
import com.aibert.dosw.infrastructure.external.feign.AcademicServiceClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AcademicServiceAdapter")
class AcademicServiceAdapterTest {

    @Mock private AcademicServiceClient client;

    @InjectMocks
    private AcademicServiceAdapter adapter;

    private final Long userId = 1L;

    @Test
    @DisplayName("getUserPerformance: DTO válido con materias en riesgo → mapea correctamente")
    void getUserPerformance_validDtoWithAtRiskSubjects_mapsCorrectly() {
        SubjectPerformanceDto calculus = new SubjectPerformanceDto(1L, "Cálculo I", 2.5, 3.0, true);
        SubjectPerformanceDto history  = new SubjectPerformanceDto(2L, "Historia", 4.0, 3.5, false);
        PerformanceDto dto = new PerformanceDto(userId, List.of(calculus, history), 3.2, true);
        when(client.getUserPerformance(userId)).thenReturn(dto);

        Optional<AcademicPerformanceData> result = adapter.getUserPerformance(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getAtRiskSubjectNames()).containsExactly("Cálculo I");
        assertThat(result.get().getOverallAverage()).isEqualTo(3.2);
        assertThat(result.get().isAtRisk()).isTrue();
    }

    @Test
    @DisplayName("getUserPerformance: DTO con subjects null → lista vacía, sin NPE")
    void getUserPerformance_nullSubjects_returnsEmptyList() {
        PerformanceDto dto = new PerformanceDto(userId, null, 3.5, false);
        when(client.getUserPerformance(userId)).thenReturn(dto);

        Optional<AcademicPerformanceData> result = adapter.getUserPerformance(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getAtRiskSubjectNames()).isEmpty();
        assertThat(result.get().isAtRisk()).isFalse();
    }

    @Test
    @DisplayName("getUserPerformance: DTO con overallAverage null → usa 0.0 por defecto")
    void getUserPerformance_nullAverage_uses0() {
        PerformanceDto dto = new PerformanceDto(userId, List.of(), null, false);
        when(client.getUserPerformance(userId)).thenReturn(dto);

        Optional<AcademicPerformanceData> result = adapter.getUserPerformance(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getOverallAverage()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("getUserPerformance: respuesta null → Optional vacío")
    void getUserPerformance_nullResponse_returnsEmpty() {
        when(client.getUserPerformance(userId)).thenReturn(null);

        Optional<AcademicPerformanceData> result = adapter.getUserPerformance(userId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getUserPerformance: ninguna materia en riesgo → lista vacía")
    void getUserPerformance_noAtRiskSubjects_returnsEmptyList() {
        SubjectPerformanceDto s1 = new SubjectPerformanceDto(1L, "Física", 4.2, 3.0, false);
        SubjectPerformanceDto s2 = new SubjectPerformanceDto(2L, "Química", 3.8, 3.0, false);
        PerformanceDto dto = new PerformanceDto(userId, List.of(s1, s2), 4.0, false);
        when(client.getUserPerformance(userId)).thenReturn(dto);

        Optional<AcademicPerformanceData> result = adapter.getUserPerformance(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getAtRiskSubjectNames()).isEmpty();
    }
}
