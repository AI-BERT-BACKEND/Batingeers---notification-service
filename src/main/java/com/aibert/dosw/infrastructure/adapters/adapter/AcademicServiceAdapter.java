package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.application.dto.external.AcademicPerformanceData;
import com.aibert.dosw.domain.ports.out.AcademicServicePort;
import com.aibert.dosw.infrastructure.external.dto.PerformanceDto;
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

        List<String> atRiskSubjects = dto.getSubjects() == null
                ? Collections.emptyList()
                : dto.getSubjects().stream()
                        .filter(s -> s.isAtRisk())
                        .map(s -> s.getSubjectName())
                        .collect(Collectors.toList());

        return Optional.of(AcademicPerformanceData.builder()
                .userId(dto.getUserId())
                .atRiskSubjectNames(atRiskSubjects)
                .overallAverage(dto.getOverallAverage() != null ? dto.getOverallAverage() : 0.0)
                .atRisk(dto.isAtRisk())
                .build());
    }
}
