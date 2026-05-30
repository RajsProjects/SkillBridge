package com.skillbridge.backend.admin.dto;

import com.skillbridge.backend.common.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserSummary {
    private UUID id;
    private String name;
    private String email;
    private UserRole role;
    private Boolean verified;
    private Boolean active;
    private LocalDateTime createdAt;
}