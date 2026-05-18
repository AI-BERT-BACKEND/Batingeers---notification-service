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
                    .title("Weekly availability not configured")
                    .message("Set up your weekly availability to receive overload alerts.")
                    .build();
        }

        WeeklyAvailabilityData availability = availabilityOpt.get();

        Optional<TaskWorkloadData> workloadOpt = taskServicePort.getUserWorkload(userId);
        if (workloadOpt.isEmpty()) {
            return OverloadAlertDTO.builder().active(false).build();
        }

        TaskWorkloadData workload = workloadOpt.get();
        double requiredHours = (double) workload.getTotalTasks() * HOURS_PER_TASK;
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
                ? "Consider rescheduling some tasks or reducing your academic load this week."
                : "Prioritize urgent tasks and reschedule lower-priority ones.";

        return OverloadAlertDTO.builder()
                .active(true)
                .bannerVariant(workload.isCritical() ? "critical" : "warning")
                .title(workload.isCritical() ? "Critical overload" : "Possible overload")
                .message(String.format(Locale.US,
                        "You have %d tasks (%.1f h estimated) vs %.1f h available this week.",
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
                .title("Low academic performance")
                .alertTitle("Subjects at academic risk")
                .message(String.format(Locale.US,
                        "Your current average is %.1f (minimum threshold: %.1f). Subjects at risk: %s.",
                        performance.getOverallAverage(), GRADE_THRESHOLD,
                        atRiskSubjects.isEmpty() ? "none" : String.join(", ", atRiskSubjects)))
                .alertMessage(alertMessage)
                .recommendation("Review each subject at risk and consult with your academic advisor.")
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
        if (grade < 2.0) return "Critical";
        if (grade < 2.5) return "High";
        return "Medium";
    }

    private String recommendationFor(double grade) {
        if (grade < 2.0) return "Seek immediate academic counseling for this subject.";
        if (grade < 2.5) return "Review missed assessments and seek tutoring support.";
        return "Dedicate more study time to this subject.";
    }

    private String buildAlertMessage(List<RiskSubjectDTO> riskSubjects, double average) {
        if (riskSubjects.isEmpty()) {
            return String.format(Locale.US,
                    "Your average of %.1f is below the threshold. Review your academic performance.", average);
        }
        String subjects = riskSubjects.stream()
                .map(s -> String.format(Locale.US, "%s (%.1f)", s.getName(), s.getProjectedGrade()))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        return String.format("You have %d subject(s) at risk: %s. Take action before the next exam.",
                riskSubjects.size(), subjects);
    }
}
