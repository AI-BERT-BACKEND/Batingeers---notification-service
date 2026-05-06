package com.aibert.dosw.application.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyInviteData {
    private Long inviteId;
    private Long fromUserId;
    private String fromUsername;
    private String subjectName;
    private String scheduledAt;
}
