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
public class PlanningEvent {
    private Long userId;
    /** STUDY_SUGGESTION or OVERLOAD_ALERT */
    private String type;
    private String title;
    private String message;
    private String severity;
    private Long relatedEntityId;
}
