package com.aibert.dosw.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cantidad de notificaciones no leídas")
public class UnreadCountResponse {

    @Schema(description = "ID del usuario", example = "1")
    private Long userId;

    @Schema(description = "Total de notificaciones no leídas", example = "3")
    private long count;
}
