package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.stats.AlertsResponseDTO;
import com.aibert.dosw.application.dto.stats.LowGradeAlertDTO;
import com.aibert.dosw.application.dto.stats.OverloadAlertDTO;
import com.aibert.dosw.config.JwtAuthenticationFilter;
import com.aibert.dosw.config.UserPrincipal;
import com.aibert.dosw.domain.ports.in.GetStatsAlertsPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = AlertController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@DisplayName("AlertController")
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetStatsAlertsPort getStatsAlertsPort;

    private UsernamePasswordAuthenticationToken auth() {
        UserPrincipal principal = new UserPrincipal(1L, "testuser");
        return new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    }

    @Test
    @DisplayName("GET /api/v1/stats/alerts → 200 con ambas alertas inactivas (sin disponibilidad)")
    void getAlerts_noAvailability_returns200BothInactive() throws Exception {
        AlertsResponseDTO response = AlertsResponseDTO.builder()
                .overloadAlert(OverloadAlertDTO.builder()
                        .active(false).title("Sin disponibilidad configurada").build())
                .lowGradeAlert(LowGradeAlertDTO.builder()
                        .active(false).threshold(3.0).subjectsAtRisk(Collections.emptyList()).build())
                .build();
        when(getStatsAlertsPort.getAlerts(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/stats/alerts")
                        .with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overloadAlert.active").value(false))
                .andExpect(jsonPath("$.lowGradeAlert.active").value(false))
                .andExpect(jsonPath("$.lowGradeAlert.threshold").value(3.0));
    }

    @Test
    @DisplayName("GET /api/v1/stats/alerts → 200 sin materias en riesgo")
    void getAlerts_noSubjectsAtRisk_returns200() throws Exception {
        AlertsResponseDTO response = AlertsResponseDTO.builder()
                .overloadAlert(OverloadAlertDTO.builder().active(false).build())
                .lowGradeAlert(LowGradeAlertDTO.builder()
                        .active(false).currentAverage(3.8).threshold(3.0)
                        .subjectsAtRisk(Collections.emptyList()).build())
                .build();
        when(getStatsAlertsPort.getAlerts(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/stats/alerts")
                        .with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lowGradeAlert.subjectsAtRisk").isArray())
                .andExpect(jsonPath("$.lowGradeAlert.currentAverage").value(3.8));
    }

    @Test
    @DisplayName("GET /api/v1/stats/alerts → 200 con ambas alertas activas")
    void getAlerts_bothActive_returns200() throws Exception {
        AlertsResponseDTO response = AlertsResponseDTO.builder()
                .overloadAlert(OverloadAlertDTO.builder()
                        .active(true).bannerVariant("critical")
                        .title("Sobrecarga crítica").requiredHours(10).availableHours(8).build())
                .lowGradeAlert(LowGradeAlertDTO.builder()
                        .active(true).bannerVariant("warning")
                        .title("Bajo rendimiento académico")
                        .currentAverage(2.8).threshold(3.0)
                        .subjectsAtRisk(List.of("Cálculo I")).build())
                .build();
        when(getStatsAlertsPort.getAlerts(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/stats/alerts")
                        .with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overloadAlert.active").value(true))
                .andExpect(jsonPath("$.overloadAlert.bannerVariant").value("critical"))
                .andExpect(jsonPath("$.overloadAlert.requiredHours").value(10))
                .andExpect(jsonPath("$.lowGradeAlert.active").value(true))
                .andExpect(jsonPath("$.lowGradeAlert.bannerVariant").value("warning"))
                .andExpect(jsonPath("$.lowGradeAlert.subjectsAtRisk[0]").value("Cálculo I"));
    }
}
