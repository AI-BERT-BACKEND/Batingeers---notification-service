package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.stats.AlertsResponseDTO;
import com.aibert.dosw.application.dto.stats.LowGradeAlertDTO;
import com.aibert.dosw.application.dto.stats.OverloadAlertDTO;
import com.aibert.dosw.application.usecase.stats.AlertService;
import com.aibert.dosw.application.usecase.stats.NotificationDispatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationDispatcher")
class NotificationDispatcherTest {

    @Mock private AlertService alertService;

    @InjectMocks
    private NotificationDispatcher dispatcher;

    private final Long userId = 1L;

    @Test
    @DisplayName("getAlerts consolida overloadAlert y lowGradeAlert en AlertsResponseDTO")
    void getAlerts_consolidatesBothAlerts() {
        OverloadAlertDTO overload = OverloadAlertDTO.builder()
                .active(true).bannerVariant("warning").title("Posible sobrecarga").build();
        LowGradeAlertDTO lowGrade = LowGradeAlertDTO.builder()
                .active(false).threshold(3.0).subjectsAtRisk(Collections.emptyList()).build();

        when(alertService.evaluateOverloadAlert(userId)).thenReturn(overload);
        when(alertService.evaluateLowGradeAlert(userId)).thenReturn(lowGrade);

        AlertsResponseDTO result = dispatcher.getAlerts(userId);

        assertThat(result.getOverloadAlert()).isEqualTo(overload);
        assertThat(result.getLowGradeAlert()).isEqualTo(lowGrade);
        verify(alertService).evaluateOverloadAlert(userId);
        verify(alertService).evaluateLowGradeAlert(userId);
    }

    @Test
    @DisplayName("getAlerts retorna ambas alertas activas cuando las condiciones lo justifican")
    void getAlerts_bothActiveAlerts() {
        OverloadAlertDTO overload = OverloadAlertDTO.builder()
                .active(true).bannerVariant("critical").weeklyTaskCount(12).build();
        LowGradeAlertDTO lowGrade = LowGradeAlertDTO.builder()
                .active(true).bannerVariant("warning").currentAverage(2.8)
                .subjectsAtRisk(List.of("Cálculo I")).threshold(3.0).build();

        when(alertService.evaluateOverloadAlert(userId)).thenReturn(overload);
        when(alertService.evaluateLowGradeAlert(userId)).thenReturn(lowGrade);

        AlertsResponseDTO result = dispatcher.getAlerts(userId);

        assertThat(result.getOverloadAlert().isActive()).isTrue();
        assertThat(result.getLowGradeAlert().isActive()).isTrue();
    }

    @Test
    @DisplayName("getAlerts retorna ambas alertas inactivas cuando no hay condiciones")
    void getAlerts_bothInactiveAlerts() {
        OverloadAlertDTO overload = OverloadAlertDTO.builder().active(false).build();
        LowGradeAlertDTO lowGrade = LowGradeAlertDTO.builder()
                .active(false).threshold(3.0).subjectsAtRisk(Collections.emptyList()).build();

        when(alertService.evaluateOverloadAlert(userId)).thenReturn(overload);
        when(alertService.evaluateLowGradeAlert(userId)).thenReturn(lowGrade);

        AlertsResponseDTO result = dispatcher.getAlerts(userId);

        assertThat(result.getOverloadAlert().isActive()).isFalse();
        assertThat(result.getLowGradeAlert().isActive()).isFalse();
    }
}
