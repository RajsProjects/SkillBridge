package com.skillbridge.backend.contract.dto;

import com.skillbridge.backend.common.enums.ContractStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ContractResponse {
    private UUID id;
    private UUID jobId;
    private String jobTitle;
    private UUID clientId;
    private String clientName;
    private UUID studentId;
    private String studentName;
    private BigDecimal amount;
    private ContractStatus status;
    private String submissionUrl;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}