package com.skillbridge.backend.payment;

import com.skillbridge.backend.common.enums.PaymentStatus;
import com.skillbridge.backend.contract.Contract;
import com.skillbridge.backend.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal commission;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal studentPayout;

    @Column(unique = true)
    private String gatewayOrderId;

    @Column(unique = true)
    private String gatewayPaymentId;

    private String gatewaySignature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal razorpayFee;       // 2% auto-deducted by Razorpay

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal netReceived;       // what actually hits your bank

    @Column(nullable = false)
    @Builder.Default
    private Boolean payoutSent = false;   // did you manually pay the student?

    private LocalDateTime payoutSentAt;

    private String payoutNote;            // e.g. "Paid via GPay to 9876543210"
}