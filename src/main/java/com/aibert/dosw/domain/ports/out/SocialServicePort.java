package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.application.dto.external.StudyInviteData;

import java.util.List;

public interface SocialServicePort {
    List<StudyInviteData> getPendingStudyInvites(Long userId);
}
