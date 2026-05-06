package com.aibert.dosw.infrastructure.external.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudyInviteDto {
    private Long inviteId;
    private Long fromUserId;
    private String fromUsername;
    private String subjectName;
    private String scheduledAt;
    private String status;
}
