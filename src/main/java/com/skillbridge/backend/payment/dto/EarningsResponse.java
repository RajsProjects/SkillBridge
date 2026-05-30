package com.skillbridge.backend.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class EarningsResponse {
    private BigDecimal totalEarned;
    private BigDecimal pendingRelease;
}