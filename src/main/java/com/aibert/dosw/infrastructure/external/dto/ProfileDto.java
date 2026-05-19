package com.aibert.dosw.infrastructure.external.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {
    private UUID userId;
    private String username;
    private String email;
    private String fullName;
    private String role;
}
