package com.skillbridge.backend.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PayoutRequest {

    @NotBlank(message = "Payout note is required")
    private String payoutNote; // e.g. "Paid ₹4400 via GPay to 9876543210"
}