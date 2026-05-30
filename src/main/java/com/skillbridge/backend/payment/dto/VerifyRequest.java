package com.skillbridge.backend.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyRequest {

    @NotBlank(message = "Order ID is required")
    private String razorpayOrderId;

    @NotBlank(message = "Payment ID is required")
    private String razorpayPaymentId;

    @NotBlank(message = "Signature is required")
    private String razorpaySignature;
}