package com.skillbridge.backend.dispute.dto;

import com.skillbridge.backend.common.enums.DisputeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class DisputeResponse {
    private UUID id;
    private UUID contractId;
    private UUID openedById;
    private String openedByName;
    private String openedByRole;
    private String reason;
    private List<String> proofUrls;
    private DisputeStatus status;
    private String resolution;
    private UUID resolvedById;
    private String resolvedByName;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
}