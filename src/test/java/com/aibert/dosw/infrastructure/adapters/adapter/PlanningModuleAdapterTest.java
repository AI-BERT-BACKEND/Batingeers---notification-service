package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.application.dto.external.PendingTaskData;
import com.aibert.dosw.application.dto.external.WeeklyAvailabilityData;
import com.aibert.dosw.infrastructure.external.dto.PendingTaskDto;
import com.aibert.dosw.infrastructure.external.dto.WeeklyAvailabilityDto;
import com.aibert.dosw.infrastructure.external.feign.PlannerFeignClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlanningModuleAdapter")
class PlanningModuleAdapterTest {

    @Mock private PlannerFeignClient client;

    @InjectMocks
    private PlanningModuleAdapter adapter;

    private final Long userId = 1L;

    // ─── getWeeklyAvailability ───────────────────────────────────────────────

    @Test
    @DisplayName("getWeeklyAvailability: respuesta válida → mapea a WeeklyAvailabilityData")
    void getWeeklyAvailability_validResponse_mapsCorrectly() {
        when(client.getWeeklyAvailability(userId))
                .thenReturn(new WeeklyAvailabilityDto(userId, 20, 5));

        Optional<WeeklyAvailabilityData> result = adapter.getWeeklyAvailability(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getTotalAvailableHours()).isEqualTo(20);
        assertThat(result.get().getConfiguredDays()).isEqualTo(5);
        assertThat(result.get().isConfigured()).isTrue();
    }

    @Test
    @DisplayName("getWeeklyAvailability: respuesta null → Optional vacío")
    void getWeeklyAvailability_nullResponse_returnsEmpty() {
        when(client.getWeeklyAvailability(userId)).thenReturn(null);

        Optional<WeeklyAvailabilityData> result = adapter.getWeeklyAvailability(userId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getWeeklyAvailability: excepción Feign → Optional vacío (fallback)")
    void getWeeklyAvailability_feignException_returnsEmpty() {
        when(client.getWeeklyAvailability(userId))
                .thenThrow(new RuntimeException("planning-service unavailable"));

        Optional<WeeklyAvailabilityData> result = adapter.getWeeklyAvailability(userId);

        assertThat(result).isEmpty();
    }

    // ─── getPendingTasks ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getPendingTasks: lista válida → mapea a PendingTaskData")
    void getPendingTasks_validList_mapsCorrectly() {
        PendingTaskDto dto = new PendingTaskDto(1L, "Parcial Cálculo", "Cálculo I",
                "HIGH", LocalDate.now().plusDays(3));
        when(client.getPendingTasks(userId)).thenReturn(List.of(dto));

        List<PendingTaskData> result = adapter.getPendingTasks(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Parcial Cálculo");
        assertThat(result.get(0).getPriority()).isEqualTo("HIGH");
        assertThat(result.get(0).getSubjectName()).isEqualTo("Cálculo I");
    }

    @Test
    @DisplayName("getPendingTasks: respuesta null → lista vacía")
    void getPendingTasks_nullResponse_returnsEmpty() {
        when(client.getPendingTasks(userId)).thenReturn(null);

        List<PendingTaskData> result = adapter.getPendingTasks(userId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getPendingTasks: excepción Feign → lista vacía (fallback)")
    void getPendingTasks_feignException_returnsEmpty() {
        when(client.getPendingTasks(userId))
                .thenThrow(new RuntimeException("planning-service unavailable"));

        List<PendingTaskData> result = adapter.getPendingTasks(userId);

        assertThat(result).isEmpty();
    }
}
