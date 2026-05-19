package com.aibert.dosw.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SocialEvent {
    private Long userId;
    /** Always STUDY_SESSION_INVITE */
    private String type;
    private String title;
    private String message;
    private String severity;
    private Long relatedEntityId;
}
