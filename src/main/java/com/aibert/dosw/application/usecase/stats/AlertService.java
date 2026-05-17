package com.aibert.dosw.application.usecase.stats;

import com.aibert.dosw.application.dto.external.AcademicPerformanceData;
import com.aibert.dosw.application.dto.external.SubjectRiskData;
import com.aibert.dosw.application.dto.external.TaskWorkloadData;
import com.aibert.dosw.application.dto.external.WeeklyAvailabilityData;
import com.aibert.dosw.application.dto.stats.LowGradeAlertDTO;
import com.aibert.dosw.application.dto.stats.OverloadAlertDTO;
import com.aibert.dosw.application.dto.stats.RiskSubjectDTO;
import com.aibert.dosw.domain.ports.out.AcademicServicePort;
import com.aibert.dosw.domain.ports.out.TaskServicePort;
import com.aibert.dosw.domain.ports.out.WeeklyAvailabilityPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    static final double GRADE_THRESHOLD = 3.0;
    private static final int HOURS_PER_TASK = 2;

    private final WeeklyAvailabilityPort weeklyAvailabilityPort;
    private final AcademicServicePort academicServicePort;
    private final TaskServicePort taskServicePort;

    public OverloadAlertDTO evaluateOverloadAlert(Long userId) {
        Optional<WeeklyAvailabilityData> availabilityOpt =
                weeklyAvailabilityPort.getWeeklyAvailability(userId);

        if (availabilityOpt.isEmpty() || !availabilityOpt.get().isConfigured()) {
            return OverloadAlertDTO.builder()
                    .active(false)
                    .title("Sin disponibilidad configurada")
                    .message("Configura tu disponibilidad semanal para recibir alertas de sobrecarga.")
                    .build();
        }

        WeeklyAvailabilityData availability = availabilityOpt.get();

        Optional<TaskWorkloadData> workloadOpt = taskServicePort.getUserWorkload(userId);
        if (workloadOpt.isEmpty()) {
            return OverloadAlertDTO.builder().active(false).build();
        }

        TaskWorkloadData workload = workloadOpt.get();
        double requiredHours = workload.getTotalTasks() * HOURS_PER_TASK;
        double availableHours = availability.getTotalAvailableHours();
        double overloadHours = requiredHours - availableHours;
        boolean overloaded = requiredHours > availableHours || workload.isHigh();

        if (!overloaded) {
            return OverloadAlertDTO.builder()
                    .active(false)
                    .requiredHours(requiredHours)
                    .availableHours(availableHours)
                    .overloadHours(overloadHours)
                    .build();
        }

        String suggestedAction = workload.isCritical()
                ? "Considera reprogramar algunas tareas o reducir tu carga académica esta semana."
                : "Prioriza las tareas urgentes y reprograma las de menor prioridad.";

        return OverloadAlertDTO.builder()
                .active(true)
                .bannerVariant(workload.isCritical() ? "critical" : "warning")
                .title(workload.isCritical() ? "Sobrecarga crítica" : "Posible sobrecarga")
                .message(String.format(Locale.US,
                        "Tienes %d tareas (%.1f h estimadas) vs %.1f h disponibles esta semana.",
                        workload.getTotalTasks(), requiredHours, availableHours))
                .suggestedAction(suggestedAction)
                .requiredHours(requiredHours)
                .availableHours(availableHours)
                .overloadHours(overloadHours)
                .build();
    }

    public LowGradeAlertDTO evaluateLowGradeAlert(Long userId) {
        Optional<AcademicPerformanceData> performanceOpt =
                academicServicePort.getUserPerformance(userId);

        if (performanceOpt.isEmpty()) {
            return LowGradeAlertDTO.builder()
                    .active(false)
                    .threshold(GRADE_THRESHOLD)
                    .subjectsAtRisk(Collections.emptyList())
                    .riskSubjects(Collections.emptyList())
                    .build();
        }

        AcademicPerformanceData performance = performanceOpt.get();
        List<String> atRiskSubjects = performance.getAtRiskSubjectNames() != null
                ? performance.getAtRiskSubjectNames()
                : Collections.emptyList();

        boolean belowThreshold = performance.isAtRisk()
                || performance.getOverallAverage() < GRADE_THRESHOLD;

        if (!belowThreshold) {
            return LowGradeAlertDTO.builder()
                    .active(false)
                    .currentAverage(performance.getOverallAverage())
                    .threshold(GRADE_THRESHOLD)
                    .subjectsAtRisk(Collections.emptyList())
                    .riskSubjects(Collections.emptyList())
                    .build();
        }

        boolean critical = performance.getOverallAverage() < (GRADE_THRESHOLD - 0.5);

        List<RiskSubjectDTO> riskSubjects = buildRiskSubjects(performance);
        String alertMessage = buildAlertMessage(riskSubjects, performance.getOverallAverage());

        return LowGradeAlertDTO.builder()
                .active(true)
                .bannerVariant(critical ? "critical" : "warning")
                .title("Bajo rendimiento académico")
                .alertTitle("Materias en riesgo académico")
                .message(String.format(Locale.US,
                        "Tu promedio actual es %.1f (umbral mínimo: %.1f). Materias en riesgo: %s.",
                        performance.getOverallAverage(), GRADE_THRESHOLD,
                        atRiskSubjects.isEmpty() ? "ninguna" : String.join(", ", atRiskSubjects)))
                .alertMessage(alertMessage)
                .recommendation("Revisa cada materia en riesgo y consulta con tu tutor académico.")
                .subjectsAtRisk(atRiskSubjects)
                .riskSubjects(riskSubjects)
                .currentAverage(performance.getOverallAverage())
                .threshold(GRADE_THRESHOLD)
                .generatedDate(LocalDateTime.now())
                .build();
    }

    private List<RiskSubjectDTO> buildRiskSubjects(AcademicPerformanceData performance) {
        List<SubjectRiskData> risks = performance.getSubjectRisks();
        if (risks == null || risks.isEmpty()) return Collections.emptyList();

        return risks.stream()
                .sorted(Comparator.comparingDouble(SubjectRiskData::getProjectedGrade))
                .map(s -> RiskSubjectDTO.builder()
                        .subjectId(s.getSubjectId())
                        .name(s.getName())
                        .projectedGrade(s.getProjectedGrade())
                        .riskLevel(riskLevelFor(s.getProjectedGrade()))
                        .recommendation(recommendationFor(s.getProjectedGrade()))
                        .build())
                .toList();
    }

    private String riskLevelFor(double grade) {
        if (grade < 2.0) return "Crítico";
        if (grade < 2.5) return "Alto";
        return "Medio";
    }

    private String recommendationFor(double grade) {
        if (grade < 2.0) return "Busca asesoría académica inmediata para esta materia.";
        if (grade < 2.5) return "Revisa las evaluaciones perdidas y busca refuerzo.";
        return "Dedica más tiempo de estudio a esta materia.";
    }

    private String buildAlertMessage(List<RiskSubjectDTO> riskSubjects, double average) {
        if (riskSubjects.isEmpty()) {
            return String.format(Locale.US,
                    "Tu promedio de %.1f está por debajo del umbral. Revisa tu rendimiento.", average);
        }
        String subjects = riskSubjects.stream()
                .map(s -> String.format(Locale.US, "%s (%.1f)", s.getName(), s.getProjectedGrade()))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        return String.format("Tienes %d materia(s) en riesgo: %s. Toma acción antes del próximo parcial.",
                riskSubjects.size(), subjects);
    }
}
