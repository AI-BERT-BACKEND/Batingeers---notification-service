package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.external.AcademicPerformanceData;
import com.aibert.dosw.application.dto.external.SubjectRiskData;
import com.aibert.dosw.application.dto.external.TaskWorkloadData;
import com.aibert.dosw.application.dto.external.WeeklyAvailabilityData;
import com.aibert.dosw.application.dto.stats.LowGradeAlertDTO;
import com.aibert.dosw.application.dto.stats.OverloadAlertDTO;
import com.aibert.dosw.application.usecase.stats.AlertService;
import com.aibert.dosw.domain.ports.out.AcademicServicePort;
import com.aibert.dosw.domain.ports.out.TaskServicePort;
import com.aibert.dosw.domain.ports.out.WeeklyAvailabilityPort;
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
@DisplayName("AlertService")
class AlertServiceTest {

    @Mock private WeeklyAvailabilityPort weeklyAvailabilityPort;
    @Mock private AcademicServicePort academicServicePort;
    @Mock private TaskServicePort taskServicePort;

    @InjectMocks
    private AlertService alertService;

    private final Long userId = 1L;

    // ─── evaluateOverloadAlert ───────────────────────────────────────────────

    @Test
    @DisplayName("Overload: sin disponibilidad configurada → inactiva con mensaje guía")
    void overload_noAvailability_returnsInactiveWithGuide() {
        when(weeklyAvailabilityPort.getWeeklyAvailability(userId)).thenReturn(Optional.empty());

        OverloadAlertDTO result = alertService.evaluateOverloadAlert(userId);

        assertThat(result.isActive()).isFalse();
        assertThat(result.getTitle()).contains("Sin disponibilidad");
    }

    @Test
    @DisplayName("Overload: configuredDays=0 → inactiva aunque haya workload")
    void overload_zeroConfiguredDays_returnsInactive() {
        when(weeklyAvailabilityPort.getWeeklyAvailability(userId)).thenReturn(Optional.of(
                WeeklyAvailabilityData.builder().userId(userId)
                        .totalAvailableHours(20).configuredDays(0).build()));

        OverloadAlertDTO result = alertService.evaluateOverloadAlert(userId);

        assertThat(result.isActive()).isFalse();
    }

    @Test
    @DisplayName("Overload: carga normal → alerta inactiva con requiredHours y overloadHours")
    void overload_normalWorkload_returnsInactive() {
        when(weeklyAvailabilityPort.getWeeklyAvailability(userId)).thenReturn(Optional.of(
                WeeklyAvailabilityData.builder().userId(userId)
                        .totalAvailableHours(20).configuredDays(5).build()));
        when(taskServicePort.getUserWorkload(userId)).thenReturn(Optional.of(
                TaskWorkloadData.builder().userId(userId).totalTasks(3)
                        .urgentTasks(1).overdueTasks(0).workloadLevel("LOW").build()));

        OverloadAlertDTO result = alertService.evaluateOverloadAlert(userId);

        assertThat(result.isActive()).isFalse();
        assertThat(result.getRequiredHours()).isEqualTo(6.0);
        assertThat(result.getAvailableHours()).isEqualTo(20.0);
        assertThat(result.getOverloadHours()).isEqualTo(-14.0);
    }

    @Test
    @DisplayName("Overload: carga HIGH → activa con variant warning y suggestedAction")
    void overload_highWorkload_returnsActiveWarning() {
        when(weeklyAvailabilityPort.getWeeklyAvailability(userId)).thenReturn(Optional.of(
                WeeklyAvailabilityData.builder().userId(userId)
                        .totalAvailableHours(10).configuredDays(5).build()));
        when(taskServicePort.getUserWorkload(userId)).thenReturn(Optional.of(
                TaskWorkloadData.builder().userId(userId).totalTasks(8)
                        .urgentTasks(3).overdueTasks(1).workloadLevel("HIGH").build()));

        OverloadAlertDTO result = alertService.evaluateOverloadAlert(userId);

        assertThat(result.isActive()).isTrue();
        assertThat(result.getBannerVariant()).isEqualTo("warning");
        assertThat(result.getMessage()).contains("8 tareas");
        assertThat(result.getSuggestedAction()).contains("Prioriza");
        assertThat(result.getRequiredHours()).isEqualTo(16.0);
        assertThat(result.getOverloadHours()).isEqualTo(6.0);
    }

    @Test
    @DisplayName("Overload: carga CRITICAL → activa con variant critical y suggestedAction de reducción")
    void overload_criticalWorkload_returnsActiveCritical() {
        when(weeklyAvailabilityPort.getWeeklyAvailability(userId)).thenReturn(Optional.of(
                WeeklyAvailabilityData.builder().userId(userId)
                        .totalAvailableHours(10).configuredDays(5).build()));
        when(taskServicePort.getUserWorkload(userId)).thenReturn(Optional.of(
                TaskWorkloadData.builder().userId(userId).totalTasks(10)
                        .urgentTasks(5).overdueTasks(3).workloadLevel("CRITICAL").build()));

        OverloadAlertDTO result = alertService.evaluateOverloadAlert(userId);

        assertThat(result.isActive()).isTrue();
        assertThat(result.getBannerVariant()).isEqualTo("critical");
        assertThat(result.getRequiredHours()).isEqualTo(20.0);
        assertThat(result.getAvailableHours()).isEqualTo(10.0);
        assertThat(result.getOverloadHours()).isEqualTo(10.0);
        assertThat(result.getSuggestedAction()).contains("reprogramar");
    }

    @Test
    @DisplayName("Overload: sin datos de workload → alerta inactiva")
    void overload_noWorkloadData_returnsInactive() {
        when(weeklyAvailabilityPort.getWeeklyAvailability(userId)).thenReturn(Optional.of(
                WeeklyAvailabilityData.builder().userId(userId)
                        .totalAvailableHours(10).configuredDays(5).build()));
        when(taskServicePort.getUserWorkload(userId)).thenReturn(Optional.empty());

        OverloadAlertDTO result = alertService.evaluateOverloadAlert(userId);

        assertThat(result.isActive()).isFalse();
    }

    // ─── evaluateLowGradeAlert ───────────────────────────────────────────────

    @Test
    @DisplayName("LowGrade: sin datos de rendimiento → inactiva con umbral 3.0")
    void lowGrade_noPerformanceData_returnsInactive() {
        when(academicServicePort.getUserPerformance(userId)).thenReturn(Optional.empty());

        LowGradeAlertDTO result = alertService.evaluateLowGradeAlert(userId);

        assertThat(result.isActive()).isFalse();
        assertThat(result.getThreshold()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("LowGrade: sin materias en riesgo y promedio >= 3.0 → inactiva")
    void lowGrade_noRiskAndGoodAverage_returnsInactive() {
        when(academicServicePort.getUserPerformance(userId)).thenReturn(Optional.of(
                AcademicPerformanceData.builder()
                        .userId(userId).atRiskSubjectNames(List.of())
                        .overallAverage(3.8).atRisk(false).build()));

        LowGradeAlertDTO result = alertService.evaluateLowGradeAlert(userId);

        assertThat(result.isActive()).isFalse();
        assertThat(result.getCurrentAverage()).isEqualTo(3.8);
        assertThat(result.getSubjectsAtRisk()).isEmpty();
    }

    @Test
    @DisplayName("LowGrade: promedio < 3.0 → activa con variant warning y alertTitle")
    void lowGrade_averageBelowThreshold_returnsActiveWarning() {
        when(academicServicePort.getUserPerformance(userId)).thenReturn(Optional.of(
                AcademicPerformanceData.builder()
                        .userId(userId).atRiskSubjectNames(List.of("Cálculo I"))
                        .overallAverage(2.8).atRisk(true).build()));

        LowGradeAlertDTO result = alertService.evaluateLowGradeAlert(userId);

        assertThat(result.isActive()).isTrue();
        assertThat(result.getBannerVariant()).isEqualTo("warning");
        assertThat(result.getSubjectsAtRisk()).contains("Cálculo I");
        assertThat(result.getMessage()).contains("2.8");
        assertThat(result.getAlertTitle()).isEqualTo("Materias en riesgo académico");
        assertThat(result.getGeneratedDate()).isNotNull();
    }

    @Test
    @DisplayName("LowGrade: promedio < 2.5 (crítico) → activa con variant critical")
    void lowGrade_criticalAverage_returnsActiveCritical() {
        when(academicServicePort.getUserPerformance(userId)).thenReturn(Optional.of(
                AcademicPerformanceData.builder()
                        .userId(userId).atRiskSubjectNames(List.of("Cálculo I", "Física II"))
                        .overallAverage(2.4).atRisk(true).build()));

        LowGradeAlertDTO result = alertService.evaluateLowGradeAlert(userId);

        assertThat(result.isActive()).isTrue();
        assertThat(result.getBannerVariant()).isEqualTo("critical");
        assertThat(result.getCurrentAverage()).isEqualTo(2.4);
        assertThat(result.getGeneratedDate()).isNotNull();
    }

    @Test
    @DisplayName("LowGrade: atRisk=true aunque promedio >= 3.0 → activa")
    void lowGrade_atRiskFlagTrue_returnsActive() {
        when(academicServicePort.getUserPerformance(userId)).thenReturn(Optional.of(
                AcademicPerformanceData.builder()
                        .userId(userId).atRiskSubjectNames(List.of("Estadística"))
                        .overallAverage(3.1).atRisk(true).build()));

        LowGradeAlertDTO result = alertService.evaluateLowGradeAlert(userId);

        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("Ambas alertas activas simultáneamente")
    void bothAlerts_allConditionsActive() {
        when(weeklyAvailabilityPort.getWeeklyAvailability(userId)).thenReturn(Optional.of(
                WeeklyAvailabilityData.builder().userId(userId)
                        .totalAvailableHours(8).configuredDays(3).build()));
        when(taskServicePort.getUserWorkload(userId)).thenReturn(Optional.of(
                TaskWorkloadData.builder().userId(userId).totalTasks(10)
                        .urgentTasks(6).overdueTasks(4).workloadLevel("CRITICAL").build()));
        when(academicServicePort.getUserPerformance(userId)).thenReturn(Optional.of(
                AcademicPerformanceData.builder()
                        .userId(userId).atRiskSubjectNames(List.of("Cálculo I"))
                        .overallAverage(2.3).atRisk(true).build()));

        OverloadAlertDTO overload  = alertService.evaluateOverloadAlert(userId);
        LowGradeAlertDTO lowGrade = alertService.evaluateLowGradeAlert(userId);

        assertThat(overload.isActive()).isTrue();
        assertThat(lowGrade.isActive()).isTrue();
    }

    // ─── riskSubjects, riskLevel & alertMessage ──────────────────────────────

    @Test
    @DisplayName("LowGrade: con subjectRisks → riskSubjects ordenados por nota ascendente")
    void lowGrade_withSubjectRisks_sortedAscending() {
        List<SubjectRiskData> risks = List.of(
                SubjectRiskData.builder().subjectId("2").name("Física II").projectedGrade(2.8).build(),
                SubjectRiskData.builder().subjectId("1").name("Cálculo I").projectedGrade(1.9).build()
        );
        when(academicServicePort.getUserPerformance(userId)).thenReturn(Optional.of(
                AcademicPerformanceData.builder()
                        .userId(userId)
                        .atRiskSubjectNames(List.of("Física II", "Cálculo I"))
                        .subjectRisks(risks)
                        .overallAverage(2.3).atRisk(true).build()));

        LowGradeAlertDTO result = alertService.evaluateLowGradeAlert(userId);

        assertThat(result.isActive()).isTrue();
        assertThat(result.getRiskSubjects()).hasSize(2);
        assertThat(result.getRiskSubjects().get(0).getName()).isEqualTo("Cálculo I");
        assertThat(result.getRiskSubjects().get(0).getProjectedGrade()).isEqualTo(1.9);
        assertThat(result.getRiskSubjects().get(1).getName()).isEqualTo("Física II");
    }

    @Test
    @DisplayName("LowGrade: con subjectRisks → cada materia tiene riskLevel correcto")
    void lowGrade_withSubjectRisks_hasCorrectRiskLevel() {
        List<SubjectRiskData> risks = List.of(
                SubjectRiskData.builder().subjectId("1").name("Cálculo I").projectedGrade(1.8).build(),
                SubjectRiskData.builder().subjectId("2").name("Física II").projectedGrade(2.3).build(),
                SubjectRiskData.builder().subjectId("3").name("Estadística").projectedGrade(2.7).build()
        );
        when(academicServicePort.getUserPerformance(userId)).thenReturn(Optional.of(
                AcademicPerformanceData.builder()
                        .userId(userId).atRiskSubjectNames(List.of("Cálculo I", "Física II", "Estadística"))
                        .subjectRisks(risks).overallAverage(2.3).atRisk(true).build()));

        LowGradeAlertDTO result = alertService.evaluateLowGradeAlert(userId);

        assertThat(result.getRiskSubjects().get(0).getRiskLevel()).isEqualTo("Crítico");
        assertThat(result.getRiskSubjects().get(1).getRiskLevel()).isEqualTo("Alto");
        assertThat(result.getRiskSubjects().get(2).getRiskLevel()).isEqualTo("Medio");
    }

    @Test
    @DisplayName("LowGrade: con subjectRisks → cada materia tiene recommendation")
    void lowGrade_withSubjectRisks_eachHasRecommendation() {
        List<SubjectRiskData> risks = List.of(
                SubjectRiskData.builder().subjectId("1").name("Cálculo I").projectedGrade(1.8).build(),
                SubjectRiskData.builder().subjectId("2").name("Física II").projectedGrade(2.3).build(),
                SubjectRiskData.builder().subjectId("3").name("Estadística").projectedGrade(2.7).build()
        );
        when(academicServicePort.getUserPerformance(userId)).thenReturn(Optional.of(
                AcademicPerformanceData.builder()
                        .userId(userId).atRiskSubjectNames(List.of("Cálculo I", "Física II", "Estadística"))
                        .subjectRisks(risks).overallAverage(2.3).atRisk(true).build()));

        LowGradeAlertDTO result = alertService.evaluateLowGradeAlert(userId);

        assertThat(result.getRiskSubjects().get(0).getRecommendation()).contains("asesoría académica");
        assertThat(result.getRiskSubjects().get(1).getRecommendation()).contains("Revisa");
        assertThat(result.getRiskSubjects().get(2).getRecommendation()).contains("tiempo de estudio");
    }

    @Test
    @DisplayName("LowGrade: con subjectRisks → cada materia tiene subjectId mapeado")
    void lowGrade_withSubjectRisks_hasSubjectId() {
        List<SubjectRiskData> risks = List.of(
                SubjectRiskData.builder().subjectId("42").name("Cálculo I").projectedGrade(2.1).build()
        );
        when(academicServicePort.getUserPerformance(userId)).thenReturn(Optional.of(
                AcademicPerformanceData.builder()
                        .userId(userId).atRiskSubjectNames(List.of("Cálculo I"))
                        .subjectRisks(risks).overallAverage(2.5).atRisk(true).build()));

        LowGradeAlertDTO result = alertService.evaluateLowGradeAlert(userId);

        assertThat(result.getRiskSubjects().get(0).getSubjectId()).isEqualTo("42");
    }

    @Test
    @DisplayName("LowGrade: con subjectRisks → alertMessage contiene nombres y notas")
    void lowGrade_withSubjectRisks_alertMessageContainsInfo() {
        List<SubjectRiskData> risks = List.of(
                SubjectRiskData.builder().subjectId("1").name("Cálculo I").projectedGrade(2.1).build()
        );
        when(academicServicePort.getUserPerformance(userId)).thenReturn(Optional.of(
                AcademicPerformanceData.builder()
                        .userId(userId).atRiskSubjectNames(List.of("Cálculo I"))
                        .subjectRisks(risks).overallAverage(2.5).atRisk(true).build()));

        LowGradeAlertDTO result = alertService.evaluateLowGradeAlert(userId);

        assertThat(result.getAlertMessage()).contains("Cálculo I");
        assertThat(result.getAlertMessage()).contains("2.1");
    }

    @Test
    @DisplayName("LowGrade: sin subjectRisks → riskSubjects vacío, alertMessage con promedio")
    void lowGrade_noSubjectRisks_emptyRiskSubjects() {
        when(academicServicePort.getUserPerformance(userId)).thenReturn(Optional.of(
                AcademicPerformanceData.builder()
                        .userId(userId).atRiskSubjectNames(List.of("Cálculo I"))
                        .subjectRisks(null).overallAverage(2.8).atRisk(true).build()));

        LowGradeAlertDTO result = alertService.evaluateLowGradeAlert(userId);

        assertThat(result.isActive()).isTrue();
        assertThat(result.getRiskSubjects()).isEmpty();
        assertThat(result.getAlertMessage()).contains("2.8");
    }
}
