package com.skillbridge.backend.job.dto;

import com.skillbridge.backend.common.enums.JobStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class JobResponse {
    private UUID id;
    private UUID clientId;
    private String clientName;
    private String clientBusiness;
    private String title;
    private String description;
    private String category;
    private List<String> skillsRequired;
    private BigDecimal budget;
    private LocalDate deadline;
    private Boolean isRemote;
    private JobStatus status;
    private LocalDateTime createdAt;
}