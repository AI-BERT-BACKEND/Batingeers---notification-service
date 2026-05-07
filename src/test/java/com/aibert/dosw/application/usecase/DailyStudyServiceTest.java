package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.external.PendingTaskData;
import com.aibert.dosw.application.dto.stats.StudySuggestionDTO;
import com.aibert.dosw.application.usecase.stats.DailyStudyService;
import com.aibert.dosw.domain.ports.out.PendingTasksPort;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DailyStudyService")
class DailyStudyServiceTest {

    @Mock private PendingTasksPort pendingTasksPort;

    @InjectMocks
    private DailyStudyService dailyStudyService;

    private final Long userId = 1L;

    @Test
    @DisplayName("sin tareas pendientes → retorna Optional vacío")
    void noPendingTasks_returnsEmpty() {
        when(pendingTasksPort.getPendingTasks(userId)).thenReturn(Collections.emptyList());

        Optional<StudySuggestionDTO> result = dailyStudyService.getSuggestion(userId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("con tareas pendientes → retorna la de mayor prioridad calculada")
    void withPendingTasks_returnsHighestPriority() {
        PendingTaskData high = PendingTaskData.builder()
                .taskId(1L).title("Tarea Urgente").subjectName("Cálculo I")
                .priority("HIGH").dueDate(LocalDate.now().plusDays(1)).build();
        PendingTaskData low = PendingTaskData.builder()
                .taskId(2L).title("Tarea Normal").subjectName("Historia")
                .priority("LOW").dueDate(LocalDate.now().plusDays(10)).build();

        when(pendingTasksPort.getPendingTasks(userId)).thenReturn(List.of(low, high));

        Optional<StudySuggestionDTO> result = dailyStudyService.getSuggestion(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getSuggestedTask()).isEqualTo("Tarea Urgente");
        assertThat(result.get().getSubjectName()).isEqualTo("Cálculo I");
    }

    @Test
    @DisplayName("empate de prioridad calculada → retorna alguna de las dos")
    void tieInPriority_returnsSomeTask() {
        PendingTaskData taskA = PendingTaskData.builder()
                .taskId(1L).title("Tarea A").subjectName("Física")
                .priority("MEDIUM").dueDate(LocalDate.now().plusDays(3)).build();
        PendingTaskData taskB = PendingTaskData.builder()
                .taskId(2L).title("Tarea B").subjectName("Química")
                .priority("MEDIUM").dueDate(LocalDate.now().plusDays(3)).build();

        when(pendingTasksPort.getPendingTasks(userId)).thenReturn(List.of(taskA, taskB));

        Optional<StudySuggestionDTO> result = dailyStudyService.getSuggestion(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getSuggestedTask()).isIn("Tarea A", "Tarea B");
    }

    @Test
    @DisplayName("fórmula peso×0.6 + 1/días×0.4 calculada correctamente para HIGH con 2 días")
    void priorityFormula_highPriority2Days_correct() {
        PendingTaskData task = PendingTaskData.builder()
                .taskId(1L).title("Test").subjectName("Álgebra")
                .priority("HIGH").dueDate(LocalDate.now().plusDays(2)).build();

        double expected = 3 * 0.6 + (1.0 / 2) * 0.4; // 1.8 + 0.2 = 2.0
        double actual = dailyStudyService.calculatePriority(task);

        assertThat(actual).isCloseTo(expected, Offset.offset(0.001));
    }

    @Test
    @DisplayName("tarea vencida (0 días) → usa divisor mínimo, resultado finito y > 0")
    void overdueTask_usesMinDivisor_resultIsFinite() {
        PendingTaskData task = PendingTaskData.builder()
                .taskId(1L).title("Vencida").subjectName("Inglés")
                .priority("HIGH").dueDate(LocalDate.now().minusDays(1)).build();

        double priority = dailyStudyService.calculatePriority(task);

        assertThat(priority).isFinite();
        assertThat(priority).isGreaterThan(0);
    }

    @Test
    @DisplayName("tarea sin fecha de vencimiento → usa 7 días por defecto")
    void taskWithNullDueDate_uses7DaysDefault() {
        PendingTaskData task = PendingTaskData.builder()
                .taskId(1L).title("Sin fecha").subjectName("Biología")
                .priority("MEDIUM").dueDate(null).build();

        double priority = dailyStudyService.calculatePriority(task);
        double expected = 2 * 0.6 + (1.0 / 7) * 0.4;

        assertThat(priority).isCloseTo(expected, Offset.offset(0.001));
    }

    @Test
    @DisplayName("StudySuggestionDTO contiene razón con la fórmula explicada")
    void suggestionDTO_containsFormulaParts() {
        PendingTaskData task = PendingTaskData.builder()
                .taskId(1L).title("Tarea X").subjectName("Biología")
                .priority("MEDIUM").dueDate(LocalDate.now().plusDays(3)).build();
        when(pendingTasksPort.getPendingTasks(userId)).thenReturn(List.of(task));

        StudySuggestionDTO suggestion = dailyStudyService.getSuggestion(userId).orElseThrow();

        assertThat(suggestion.getSuggestionReason()).contains("×0.6");
        assertThat(suggestion.getSuggestionReason()).contains("×0.4");
        assertThat(suggestion.getSubjectName()).isEqualTo("Biología");
        assertThat(suggestion.getPriority()).isGreaterThan(0);
    }
}
