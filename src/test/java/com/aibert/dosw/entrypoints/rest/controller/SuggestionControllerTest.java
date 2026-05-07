package com.aibert.dosw.entrypoints.rest.controller;

import com.aibert.dosw.application.dto.stats.StudySuggestionDTO;
import com.aibert.dosw.config.JwtAuthenticationFilter;
import com.aibert.dosw.config.UserPrincipal;
import com.aibert.dosw.domain.ports.in.GetDailySuggestionPort;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = SuggestionController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@DisplayName("SuggestionController")
class SuggestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetDailySuggestionPort getDailySuggestionPort;

    private UsernamePasswordAuthenticationToken auth() {
        UserPrincipal principal = new UserPrincipal(1L, "testuser");
        return new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    }

    @Test
    @DisplayName("GET /api/v1/stats/suggestion → 200 con sugerencia cuando hay tareas pendientes")
    void getSuggestion_withPendingTasks_returns200() throws Exception {
        StudySuggestionDTO dto = StudySuggestionDTO.builder()
                .suggestedTask("Resolver ejercicios de Cálculo")
                .suggestionReason("Prioridad 2.00: peso=3 (×0.6) + urgencia 1/2.0 días (×0.4).")
                .subjectName("Cálculo I")
                .priority(2.0)
                .dueDate(LocalDate.now().plusDays(2))
                .build();
        when(getDailySuggestionPort.getSuggestion(any())).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/v1/stats/suggestion")
                        .with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestedTask").value("Resolver ejercicios de Cálculo"))
                .andExpect(jsonPath("$.subjectName").value("Cálculo I"))
                .andExpect(jsonPath("$.priority").value(2.0))
                .andExpect(jsonPath("$.suggestionReason").value(
                        "Prioridad 2.00: peso=3 (×0.6) + urgencia 1/2.0 días (×0.4)."));
    }

    @Test
    @DisplayName("GET /api/v1/stats/suggestion → 204 cuando no hay tareas pendientes")
    void getSuggestion_noTasks_returns204() throws Exception {
        when(getDailySuggestionPort.getSuggestion(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/stats/suggestion")
                        .with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/stats/suggestion sin autenticación → 401")
    void getSuggestion_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/stats/suggestion")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
