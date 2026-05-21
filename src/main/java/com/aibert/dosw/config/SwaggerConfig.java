package com.aibert.dosw.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_SCHEME = "Bearer Authentication";

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API — AIBERT")
                        .description("""
                                Microservice responsible for managing academic notifications within the \
                                Intelligent University Planner (AIBERT).

                                **Key responsibilities:**
                                - **R22 — Overload alert:** compares estimated task hours \
                                (totalTasks × 2 h) against the student's configured weekly availability. \
                                Triggers a `warning` or `critical` banner when required hours exceed available hours.
                                - **R22 — Low-grade alert:** evaluates projected subject grades against a \
                                minimum threshold of 3.0. Returns per-subject risk levels (Medium / High / Critical) \
                                and actionable recommendations.
                                - **R23 — Daily study suggestion:** applies the priority formula \
                                `weight×0.6 + (1/days)×0.4` over pending tasks and returns the highest-priority \
                                task/subject pair.
                                - **Notification inbox:** stores, retrieves, and marks notifications as read \
                                for the authenticated user.
                                - **Kafka consumers (event-driven):** listens on five independent topics — \
                                `task.events` (OVERLOAD_ALERT, TASK_REMINDER), `academic.events` (LOW_PERFORMANCE_ALERT), \
                                `planning.events` (STUDY_SUGGESTION, OVERLOAD_ALERT), \
                                `social.events` (STUDY_SESSION_INVITE, CONNECTION_REQUEST_RECEIVED, CONNECTION_REQUEST_ACCEPTED, NEW_CHAT_MESSAGE), \
                                and `gamification.events` (LEVEL_UP). Each source microservice publishes events; \
                                the notification service persists them without making synchronous calls.
                                - **Profile integration (Feign):** connects to profile-service \
                                (`GET /api/v1/profiles/{userId}`) to resolve user context when needed.

                                **Authentication:** all endpoints require a Bearer JWT token issued by the \
                                auth service. Use the Authorize button to set your token.

                                **Local testing:** see `swagger-tests/swagger-tests-guide.md` in the repository \
                                for ready-to-paste curl commands and a JWT token generation guide.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Batingeers Team")
                                .email("batingeers@aibert.edu")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME,
                                new SecurityScheme()
                                        .name(BEARER_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
