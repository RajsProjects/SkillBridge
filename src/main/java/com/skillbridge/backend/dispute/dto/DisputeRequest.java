package com.skillbridge.backend.dispute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DisputeRequest {

    @NotNull(message = "Contract ID is required")
    private UUID contractId;

    @NotBlank(message = "Reason is required")
    @Size(min = 20, message = "Please describe the issue in at least 20 characters")
    private String reason;

    private List<String> proofUrls;
}