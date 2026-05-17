package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.application.dto.external.AcademicPerformanceData;
import com.aibert.dosw.application.dto.external.SubjectRiskData;
import com.aibert.dosw.domain.ports.out.AcademicServicePort;
import com.aibert.dosw.infrastructure.external.dto.PerformanceDto;
import com.aibert.dosw.infrastructure.external.dto.SubjectPerformanceDto;
import com.aibert.dosw.infrastructure.external.feign.AcademicServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AcademicServiceAdapter implements AcademicServicePort {

    private final AcademicServiceClient client;

    @Override
    public Optional<AcademicPerformanceData> getUserPerformance(Long userId) {
        PerformanceDto dto = client.getUserPerformance(userId);
        if (dto == null) return Optional.empty();

        List<SubjectPerformanceDto> subjects = dto.getSubjects() != null
                ? dto.getSubjects()
                : Collections.emptyList();

        List<String> atRiskSubjects = subjects.stream()
                .filter(SubjectPerformanceDto::isAtRisk)
                .map(SubjectPerformanceDto::getSubjectName)
                .collect(Collectors.toList());

        List<SubjectRiskData> subjectRisks = subjects.stream()
                .filter(SubjectPerformanceDto::isAtRisk)
                .map(s -> SubjectRiskData.builder()
                        .subjectId(s.getSubjectId() != null ? String.valueOf(s.getSubjectId()) : null)
                        .name(s.getSubjectName())
                        .projectedGrade(resolveProjectedGrade(s))
                        .build())
                .collect(Collectors.toList());

        return Optional.of(AcademicPerformanceData.builder()
                .userId(dto.getUserId())
                .atRiskSubjectNames(atRiskSubjects)
                .subjectRisks(subjectRisks)
                .overallAverage(dto.getOverallAverage() != null ? dto.getOverallAverage() : 0.0)
                .atRisk(dto.isAtRisk())
                .build());
    }

    private double resolveProjectedGrade(SubjectPerformanceDto s) {
        if (s.getProjectedGrade() != null) return s.getProjectedGrade();
        if (s.getCurrentGrade() != null) return s.getCurrentGrade();
        return 0.0;
    }
}
