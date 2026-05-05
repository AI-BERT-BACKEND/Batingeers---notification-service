package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.application.dto.external.AcademicPerformanceData;

import java.util.Optional;

public interface AcademicServicePort {
    Optional<AcademicPerformanceData> getUserPerformance(Long userId);
}
