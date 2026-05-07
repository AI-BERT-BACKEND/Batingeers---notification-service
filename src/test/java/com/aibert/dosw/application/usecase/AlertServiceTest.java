package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.external.AcademicPerformanceData;
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
    @DisplayName("Overload: carga normal → alerta inactiva")
    void overload_normalWorkload_returnsInactive() {
        when(weeklyAvailabilityPort.getWeeklyAvailability(userId)).thenReturn(Optional.of(
                WeeklyAvailabilityData.builder().userId(userId)
                        .totalAvailableHours(20).configuredDays(5).build()));
        when(taskServicePort.getUserWorkload(userId)).thenReturn(Optional.of(
                TaskWorkloadData.builder().userId(userId).totalTasks(3)
                        .urgentTasks(1).overdueTasks(0).workloadLevel("LOW").build()));

        OverloadAlertDTO result = alertService.evaluateOverloadAlert(userId);

        assertThat(result.isActive()).isFalse();
        assertThat(result.getWeeklyTaskCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Overload: carga HIGH → activa con variant warning")
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
    }

    @Test
    @DisplayName("Overload: carga CRITICAL → activa con variant critical")
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
        assertThat(result.getWeeklyTaskCount()).isEqualTo(10);
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
    @DisplayName("LowGrade: promedio < 3.0 → activa con variant warning")
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
}
