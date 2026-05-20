package com.aibert.dosw.config;

import com.aibert.dosw.domain.model.notification.NotificationSeverity;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.NotificationEntity;
import com.aibert.dosw.infrastructure.adapters.persistence.repository.NotificationJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final NotificationJpaRepository repository;

    private static final UUID SEED_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    public void run(ApplicationArguments args) {
        if (!repository.findByUserIdOrderByCreatedAtDesc(SEED_USER_ID).isEmpty()) {
            log.info("Seed data already present, skipping initialization.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        List<NotificationEntity> seeds = List.of(
                NotificationEntity.builder()
                        .userId(SEED_USER_ID)
                        .type(NotificationType.OVERLOAD_ALERT)
                        .title("Possible overload")
                        .message("You have 8 tasks (16.0 h estimated) vs 10.0 h available this week.")
                        .read(false)
                        .severity(NotificationSeverity.HIGH)
                        .createdAt(now.minusDays(1))
                        .build(),
                NotificationEntity.builder()
                        .userId(SEED_USER_ID)
                        .type(NotificationType.LOW_PERFORMANCE_ALERT)
                        .title("Low academic performance")
                        .message("Your current average is 2.8 (minimum threshold: 3.0). Subjects at risk: Calculus I.")
                        .read(false)
                        .severity(NotificationSeverity.HIGH)
                        .createdAt(now.minusHours(5))
                        .build(),
                NotificationEntity.builder()
                        .userId(SEED_USER_ID)
                        .type(NotificationType.STUDY_SUGGESTION)
                        .title("Study suggestion")
                        .message("You have 3 suggested tasks for today. Remember to review Calculus I.")
                        .read(true)
                        .severity(NotificationSeverity.INFO)
                        .createdAt(now.minusDays(2))
                        .readAt(now.minusDays(2).plusHours(1))
                        .build(),
                NotificationEntity.builder()
                        .userId(SEED_USER_ID)
                        .type(NotificationType.TASK_REMINDER)
                        .title("Task reminder")
                        .message("The task 'Algebra Workshop' is due tomorrow.")
                        .read(false)
                        .severity(NotificationSeverity.MEDIUM)
                        .createdAt(now.minusHours(2))
                        .build(),
                NotificationEntity.builder()
                        .userId(SEED_USER_ID)
                        .type(NotificationType.STUDY_SESSION_INVITE)
                        .title("Study session invitation")
                        .message("Carlos Lopez has invited you to a Calculus I study session on Friday at 4 PM.")
                        .read(true)
                        .severity(NotificationSeverity.LOW)
                        .createdAt(now.minusDays(3))
                        .readAt(now.minusDays(3).plusMinutes(30))
                        .build()
        );

        repository.saveAll(seeds);
        log.info("Seed data initialized: {} notifications created for userId={}", seeds.size(), SEED_USER_ID);
    }
}
