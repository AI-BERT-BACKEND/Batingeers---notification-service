package com.aibert.dosw.config;

import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.NotificationEntity;
import com.aibert.dosw.infrastructure.adapters.persistence.repository.NotificationJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataInitializer")
class DataInitializerTest {

    @Mock
    private NotificationJpaRepository repository;

    @Mock
    private ApplicationArguments args;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    @DisplayName("skips seed when data already exists")
    void shouldSkipWhenDataAlreadyExists() throws Exception {
        when(repository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(NotificationEntity.builder()
                        .userId(1L)
                        .type(NotificationType.TASK_REMINDER)
                        .title("existing")
                        .message("already here")
                        .build()));

        dataInitializer.run(args);

        verify(repository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("saves 5 seed notifications when repository is empty")
    void shouldSeedFiveNotificationsWhenEmpty() throws Exception {
        when(repository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Collections.emptyList());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<NotificationEntity>> captor = ArgumentCaptor.forClass(List.class);

        dataInitializer.run(args);

        verify(repository).saveAll(captor.capture());
        List<NotificationEntity> saved = captor.getValue();

        assertThat(saved).hasSize(5);
        assertThat(saved).extracting(NotificationEntity::getType)
                .containsExactlyInAnyOrder(
                        NotificationType.OVERLOAD_ALERT,
                        NotificationType.LOW_PERFORMANCE_ALERT,
                        NotificationType.STUDY_SUGGESTION,
                        NotificationType.TASK_REMINDER,
                        NotificationType.STUDY_SESSION_INVITE
                );
        assertThat(saved).allMatch(n -> n.getUserId().equals(1L));
        assertThat(saved).allMatch(n -> n.getTitle() != null && !n.getTitle().isBlank());
        assertThat(saved).allMatch(n -> n.getMessage() != null && !n.getMessage().isBlank());
    }
}
