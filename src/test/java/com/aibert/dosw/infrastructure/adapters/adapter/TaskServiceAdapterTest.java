package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.application.dto.external.TaskWorkloadData;
import com.aibert.dosw.infrastructure.external.dto.WorkloadDto;
import com.aibert.dosw.infrastructure.external.feign.TaskServiceClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskServiceAdapter")
class TaskServiceAdapterTest {

    @Mock private TaskServiceClient client;

    @InjectMocks
    private TaskServiceAdapter adapter;

    private final Long userId = 1L;

    @Test
    @DisplayName("getUserWorkload: DTO válido → mapea todos los campos correctamente")
    void getUserWorkload_validDto_mapsAllFields() {
        WorkloadDto dto = new WorkloadDto(userId, 8, 3, 2, "HIGH");
        when(client.getUserWorkload(userId)).thenReturn(dto);

        Optional<TaskWorkloadData> result = adapter.getUserWorkload(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getTotalTasks()).isEqualTo(8);
        assertThat(result.get().getUrgentTasks()).isEqualTo(3);
        assertThat(result.get().getOverdueTasks()).isEqualTo(2);
        assertThat(result.get().isHigh()).isTrue();
        assertThat(result.get().isCritical()).isFalse();
    }

    @Test
    @DisplayName("getUserWorkload: workloadLevel CRITICAL → isCritical y isHigh son true")
    void getUserWorkload_criticalLevel_flagsAreTrue() {
        WorkloadDto dto = new WorkloadDto(userId, 15, 7, 5, "CRITICAL");
        when(client.getUserWorkload(userId)).thenReturn(dto);

        Optional<TaskWorkloadData> result = adapter.getUserWorkload(userId);

        assertThat(result).isPresent();
        assertThat(result.get().isCritical()).isTrue();
        assertThat(result.get().isHigh()).isTrue();
    }

    @Test
    @DisplayName("getUserWorkload: workloadLevel LOW → isCritical y isHigh son false")
    void getUserWorkload_lowLevel_flagsAreFalse() {
        WorkloadDto dto = new WorkloadDto(userId, 2, 0, 0, "LOW");
        when(client.getUserWorkload(userId)).thenReturn(dto);

        Optional<TaskWorkloadData> result = adapter.getUserWorkload(userId);

        assertThat(result).isPresent();
        assertThat(result.get().isCritical()).isFalse();
        assertThat(result.get().isHigh()).isFalse();
    }

    @Test
    @DisplayName("getUserWorkload: respuesta null → Optional vacío")
    void getUserWorkload_nullResponse_returnsEmpty() {
        when(client.getUserWorkload(userId)).thenReturn(null);

        Optional<TaskWorkloadData> result = adapter.getUserWorkload(userId);

        assertThat(result).isEmpty();
    }
}
