package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.application.dto.external.TodayPlanData;
import com.aibert.dosw.infrastructure.external.dto.DailyPlanDto;
import com.aibert.dosw.infrastructure.external.dto.DailyTaskDto;
import com.aibert.dosw.infrastructure.external.feign.PlanningServiceClient;
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
@DisplayName("PlanningServiceAdapter")
class PlanningServiceAdapterTest {

    @Mock private PlanningServiceClient client;

    @InjectMocks
    private PlanningServiceAdapter adapter;

    private final Long userId = 1L;

    @Test
    @DisplayName("getTodayPlan: DTO válido con tareas → extrae títulos y mapea campos")
    void getTodayPlan_validDto_mapsCorrectly() {
        DailyTaskDto task1 = new DailyTaskDto(1L, "Parcial Cálculo", "Cálculo I", 90, "HIGH");
        DailyTaskDto task2 = new DailyTaskDto(2L, "Leer capítulo", "Historia", 30, "LOW");
        DailyPlanDto dto = new DailyPlanDto(userId, List.of(task1, task2), 120, false, "Enfócate hoy");
        when(client.getUserTodayPlan(userId)).thenReturn(dto);

        Optional<TodayPlanData> result = adapter.getTodayPlan(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getSuggestedTaskTitles())
                .containsExactly("Parcial Cálculo", "Leer capítulo");
        assertThat(result.get().getTotalEstimatedMinutes()).isEqualTo(120);
        assertThat(result.get().getFocusMessage()).isEqualTo("Enfócate hoy");
        assertThat(result.get().isOverloaded()).isFalse();
    }

    @Test
    @DisplayName("getTodayPlan: DTO con suggestedTasks null → lista vacía sin NPE")
    void getTodayPlan_nullTasks_returnsEmptyList() {
        DailyPlanDto dto = new DailyPlanDto(userId, null, 0, false, null);
        when(client.getUserTodayPlan(userId)).thenReturn(dto);

        Optional<TodayPlanData> result = adapter.getTodayPlan(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getSuggestedTaskTitles()).isEmpty();
    }

    @Test
    @DisplayName("getTodayPlan: respuesta null → Optional vacío")
    void getTodayPlan_nullResponse_returnsEmpty() {
        when(client.getUserTodayPlan(userId)).thenReturn(null);

        Optional<TodayPlanData> result = adapter.getTodayPlan(userId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getTodayPlan: plan con overloaded=true → se propaga el flag")
    void getTodayPlan_overloadedTrue_flagPropagated() {
        DailyPlanDto dto = new DailyPlanDto(userId, List.of(), 0, true, "Demasiadas tareas");
        when(client.getUserTodayPlan(userId)).thenReturn(dto);

        Optional<TodayPlanData> result = adapter.getTodayPlan(userId);

        assertThat(result).isPresent();
        assertThat(result.get().isOverloaded()).isTrue();
    }
}
