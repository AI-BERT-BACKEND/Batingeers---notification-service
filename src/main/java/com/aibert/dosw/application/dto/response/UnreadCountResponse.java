package com.aibert.dosw.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Count of unread notifications for a specific user")
public class UnreadCountResponse {

    @Schema(description = "ID of the user whose unread notifications were counted", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private UUID userId;

    @Schema(description = "Total number of unread notifications for this user", example = "3")
    private long count;
}
