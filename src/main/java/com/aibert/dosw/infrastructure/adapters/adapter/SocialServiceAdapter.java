package com.aibert.dosw.infrastructure.adapters.adapter;

import com.aibert.dosw.application.dto.external.StudyInviteData;
import com.aibert.dosw.domain.ports.out.SocialServicePort;
import com.aibert.dosw.infrastructure.external.dto.StudyInviteDto;
import com.aibert.dosw.infrastructure.external.feign.SocialServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SocialServiceAdapter implements SocialServicePort {

    private final SocialServiceClient client;

    @Override
    public List<StudyInviteData> getPendingStudyInvites(Long userId) {
        List<StudyInviteDto> dtos = client.getPendingStudyInvites(userId);
        if (dtos == null) return Collections.emptyList();
        return dtos.stream()
                .map(dto -> StudyInviteData.builder()
                        .inviteId(dto.getInviteId())
                        .fromUserId(dto.getFromUserId())
                        .fromUsername(dto.getFromUsername())
                        .subjectName(dto.getSubjectName())
                        .scheduledAt(dto.getScheduledAt())
                        .build())
                .collect(Collectors.toList());
    }
}
