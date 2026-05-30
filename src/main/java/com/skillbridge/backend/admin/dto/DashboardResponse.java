package com.skillbridge.backend.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardResponse {
    private long totalUsers;
    private long totalStudents;
    private long totalClients;
    private long totalJobs;
    private long openJobs;
    private long totalContracts;
    private long activeContracts;
    private long completedContracts;
    private long totalDisputes;
    private long openDisputes;
    private BigDecimal totalRevenue;
}