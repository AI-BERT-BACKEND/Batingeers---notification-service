package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.application.dto.external.AcademicPerformanceData;
import com.aibert.dosw.application.dto.external.SubjectRiskData;
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

    // constructor order: (subjectId, subjectName, currentGrade, targetGrade, projectedGrade, atRisk)

    @Test
    @DisplayName("getUserPerformance: DTO válido con materias en riesgo → mapea atRiskSubjectNames")
    void getUserPerformance_validDtoWithAtRiskSubjects_mapsCorrectly() {
        SubjectPerformanceDto calculus = new SubjectPerformanceDto(1L, "Cálculo I", 2.5, 3.0, 2.4, true);
        SubjectPerformanceDto history  = new SubjectPerformanceDto(2L, "Historia", 4.0, 3.5, 4.1, false);
        PerformanceDto dto = new PerformanceDto(userId, List.of(calculus, history), 3.2, true);
        when(client.getUserPerformance(userId)).thenReturn(dto);

        Optional<AcademicPerformanceData> result = adapter.getUserPerformance(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getAtRiskSubjectNames()).containsExactly("Cálculo I");
        assertThat(result.get().getOverallAverage()).isEqualTo(3.2);
        assertThat(result.get().isAtRisk()).isTrue();
    }

    @Test
    @DisplayName("getUserPerformance: materia en riesgo → subjectRisks mapeado con subjectId y projectedGrade")
    void getUserPerformance_atRiskWithProjectedGrade_mapsSubjectRisksAndId() {
        SubjectPerformanceDto calculus = new SubjectPerformanceDto(1L, "Cálculo I", 2.5, 3.0, 2.4, true);
        SubjectPerformanceDto history  = new SubjectPerformanceDto(2L, "Historia", 4.0, 3.5, 4.1, false);
        PerformanceDto dto = new PerformanceDto(userId, List.of(calculus, history), 3.2, true);
        when(client.getUserPerformance(userId)).thenReturn(dto);

        Optional<AcademicPerformanceData> result = adapter.getUserPerformance(userId);

        assertThat(result).isPresent();
        List<SubjectRiskData> risks = result.get().getSubjectRisks();
        assertThat(risks).hasSize(1);
        assertThat(risks.get(0).getName()).isEqualTo("Cálculo I");
        assertThat(risks.get(0).getProjectedGrade()).isEqualTo(2.4);
        assertThat(risks.get(0).getSubjectId()).isEqualTo("1");
    }

    @Test
    @DisplayName("getUserPerformance: projectedGrade null → usa currentGrade como fallback")
    void getUserPerformance_nullProjectedGrade_fallsBackToCurrentGrade() {
        SubjectPerformanceDto s = new SubjectPerformanceDto(1L, "Física", 2.6, 3.0, null, true);
        PerformanceDto dto = new PerformanceDto(userId, List.of(s), 2.6, true);
        when(client.getUserPerformance(userId)).thenReturn(dto);

        Optional<AcademicPerformanceData> result = adapter.getUserPerformance(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getSubjectRisks().get(0).getProjectedGrade()).isEqualTo(2.6);
    }

    @Test
    @DisplayName("getUserPerformance: DTO con subjects null → listas vacías, sin NPE")
    void getUserPerformance_nullSubjects_returnsEmptyLists() {
        PerformanceDto dto = new PerformanceDto(userId, null, 3.5, false);
        when(client.getUserPerformance(userId)).thenReturn(dto);

        Optional<AcademicPerformanceData> result = adapter.getUserPerformance(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getAtRiskSubjectNames()).isEmpty();
        assertThat(result.get().getSubjectRisks()).isEmpty();
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
    @DisplayName("getUserPerformance: ninguna materia en riesgo → listas vacías")
    void getUserPerformance_noAtRiskSubjects_returnsEmptyLists() {
        SubjectPerformanceDto s1 = new SubjectPerformanceDto(1L, "Física", 4.2, 3.0, 4.0, false);
        SubjectPerformanceDto s2 = new SubjectPerformanceDto(2L, "Química", 3.8, 3.0, 3.9, false);
        PerformanceDto dto = new PerformanceDto(userId, List.of(s1, s2), 4.0, false);
        when(client.getUserPerformance(userId)).thenReturn(dto);

        Optional<AcademicPerformanceData> result = adapter.getUserPerformance(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getAtRiskSubjectNames()).isEmpty();
        assertThat(result.get().getSubjectRisks()).isEmpty();
    }
}
