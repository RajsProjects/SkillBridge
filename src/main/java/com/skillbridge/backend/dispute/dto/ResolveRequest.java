package com.skillbridge.backend.dispute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResolveRequest {

    @NotBlank(message = "Resolution is required")
    private String resolution;

    @NotNull(message = "Release to student flag is required")
    private Boolean releaseToStudent;
}