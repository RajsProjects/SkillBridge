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

    Page<Payment> findByStatusAndPayoutSent(
            PaymentStatus status, Boolean payoutSent, Pageable pageable);

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

    // ── Optimized fetch queries (no N+1) ──────────────────

    @Query("""
        SELECT p FROM Payment p
        JOIN FETCH p.contract c
        JOIN FETCH c.job
        JOIN FETCH p.client
        JOIN FETCH p.student
        WHERE p.student.id = :studentId
    """)
    Page<Payment> findByStudentIdFetched(
            @Param("studentId") UUID studentId, Pageable pageable);

    @Query("""
        SELECT p FROM Payment p
        JOIN FETCH p.contract c
        JOIN FETCH c.job
        JOIN FETCH p.client
        JOIN FETCH p.student
        WHERE p.client.id = :clientId
    """)
    Page<Payment> findByClientIdFetched(
            @Param("clientId") UUID clientId, Pageable pageable);

    @Query("""
        SELECT p FROM Payment p
        JOIN FETCH p.contract c
        JOIN FETCH c.job
        JOIN FETCH p.client
        JOIN FETCH p.student
        WHERE p.status = :status
        AND p.payoutSent = :payoutSent
    """)
    Page<Payment> findByStatusAndPayoutSentFetched(
            @Param("status") PaymentStatus status,
            @Param("payoutSent") Boolean payoutSent,
            Pageable pageable);
}