package com.skillbridge.backend.payment.dto;

import com.skillbridge.backend.common.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentBillResponse {
    private UUID paymentId;
    private UUID contractId;
    private String jobTitle;
    private String clientName;
    private String studentName;

    // Bill breakdown
    private BigDecimal clientPaid;       // ₹5000
    private BigDecimal razorpayFee;      // ₹100  (2%)
    private BigDecimal netReceived;      // ₹4900 (hits your bank)
    private BigDecimal yourCommission;   // ₹500  (10% of original)
    private BigDecimal studentPayout;    // ₹4400 (you owe student)

    // Payout status
    private PaymentStatus status;
    private Boolean payoutSent;
    private String payoutNote;
    private LocalDateTime payoutSentAt;
    private LocalDateTime createdAt;
}