package com.skillbridge.backend.payment;

import com.skillbridge.backend.common.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByContractId(UUID contractId);
    Optional<Payment> findByGatewayOrderId(String orderId);
    Optional<Payment> findByGatewayPaymentId(String paymentId);

    Page<Payment> findByClientId(UUID clientId, Pageable pageable);
    Page<Payment> findByStudentId(UUID studentId, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(p.studentPayout), 0)
        FROM Payment p
        WHERE p.student.id = :studentId
        AND p.status = 'RELEASED'
    """)
    BigDecimal getTotalEarningsByStudentId(@Param("studentId") UUID studentId);

    @Query("""
        SELECT COALESCE(SUM(p.studentPayout), 0)
        FROM Payment p
        WHERE p.student.id = :studentId
        AND p.status = 'HELD'
    """)
    BigDecimal getPendingEarningsByStudentId(@Param("studentId") UUID studentId);

    Page<Payment> findByStatusAndPayoutSent(
            PaymentStatus status, Boolean payoutSent, Pageable pageable);
}