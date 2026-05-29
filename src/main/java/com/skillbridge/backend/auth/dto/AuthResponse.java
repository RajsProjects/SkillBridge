package com.skillbridge.backend.auth.dto;

import com.skillbridge.backend.common.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private UUID userId;
    private String name;
    private String email;
    private UserRole role;
    private String accessToken;
    private String refreshToken;
}