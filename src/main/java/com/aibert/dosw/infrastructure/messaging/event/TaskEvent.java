package com.aibert.dosw.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskEvent {
    private UUID userId;
    /** OVERLOAD_ALERT or TASK_REMINDER */
    private String type;
    private String title;
    private String message;
    private String severity;
    private Long relatedEntityId;
}
