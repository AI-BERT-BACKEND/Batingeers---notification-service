package com.aibert.dosw.application.usecase.notification;

import com.aibert.dosw.application.dto.response.NotificationResponse;
import com.aibert.dosw.application.mapper.NotificationMapper;
import com.aibert.dosw.domain.model.notification.NotificationType;
import com.aibert.dosw.domain.ports.in.GetStudySuggestionsPort;
import com.aibert.dosw.domain.ports.out.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetStudySuggestionsUseCase implements GetStudySuggestionsPort {

    private final NotificationRepositoryPort repository;
    private final NotificationMapper mapper;

    @Override
    public List<NotificationResponse> getTodaySuggestions(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return mapper.toResponseList(
                repository.findByUserIdAndTypeAndCreatedAtAfter(
                        userId, NotificationType.STUDY_SUGGESTION, startOfDay));
    }
}
