package com.skillbridge.backend.application.dto;

import com.skillbridge.backend.common.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ApplicationResponse {
    private UUID id;
    private UUID jobId;
    private String jobTitle;
    private UUID studentId;
    private String studentName;
    private String studentCollege;
    private String proposal;
    private BigDecimal priceQuote;
    private Integer deliveryDays;
    private ApplicationStatus status;
    private LocalDateTime createdAt;
}