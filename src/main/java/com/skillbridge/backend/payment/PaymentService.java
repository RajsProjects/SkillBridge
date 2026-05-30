package com.skillbridge.backend.payment;

import com.skillbridge.backend.common.enums.ContractStatus;
import com.skillbridge.backend.common.enums.PaymentStatus;
import com.skillbridge.backend.common.exception.BadRequestException;
import com.skillbridge.backend.common.exception.ResourceNotFoundException;
import com.skillbridge.backend.common.exception.UnauthorizedException;
import com.skillbridge.backend.contract.Contract;
import com.skillbridge.backend.contract.ContractRepository;
import com.skillbridge.backend.payment.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final BigDecimal COMMISSION_RATE   = new BigDecimal("0.10");  // 10%
    private static final BigDecimal RAZORPAY_FEE_RATE = new BigDecimal("0.02");  // 2% Razorpay cut
    private static final String CURRENCY = "INR";

    private final PaymentRepository paymentRepository;
    private final ContractRepository contractRepository;
    private final RazorpayClient razorpayClient;

    @Transactional
    public OrderResponse createOrder(UUID clientId, OrderRequest request) {
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        if (!contract.getClient().getId().equals(clientId)) {
            throw new UnauthorizedException("You don't own this contract");
        }

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new BadRequestException("Contract is not in active state");
        }

        if (paymentRepository.findByContractId(contract.getId()).isPresent()) {
            throw new BadRequestException("Payment already created for this contract");
        }

        BigDecimal amount     = contract.getAmount();
        BigDecimal razorpayFee = amount.multiply(RAZORPAY_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netReceived = amount.subtract(razorpayFee);
        BigDecimal commission = amount.multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal payout     = amount.subtract(commission);

        // Razorpay needs amount in paise (1 INR = 100 paise)
        long amountInPaise = amount.multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, Object> order = razorpayClient.createOrder(
                amountInPaise, CURRENCY, "contract_" + contract.getId());

        String razorpayOrderId = (String) order.get("id");

        Payment payment = Payment.builder()
                .contract(contract)
                .client(contract.getClient())
                .student(contract.getStudent())
                .amount(amount)
                .razorpayFee(razorpayFee)
                .netReceived(netReceived)
                .commission(commission)
                .studentPayout(payout)
                .gatewayOrderId(razorpayOrderId)
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);
        log.info("Payment order created: {} for contract: {}", razorpayOrderId, contract.getId());

        return OrderResponse.builder()
                .paymentId(payment.getId())
                .razorpayOrderId(razorpayOrderId)
                .amount(amount)
                .commission(commission)
                .studentPayout(payout)
                .currency(CURRENCY)
                .build();
    }

    @Transactional
    public void verifyAndHold(UUID clientId, VerifyRequest request) {
        Payment payment = paymentRepository
                .findByGatewayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (!payment.getClient().getId().equals(clientId)) {
            throw new UnauthorizedException("Unauthorized payment verification");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Payment already processed");
        }

        boolean valid = razorpayClient.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!valid) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new BadRequestException("Payment signature verification failed");
        }

        payment.setGatewayPaymentId(request.getRazorpayPaymentId());
        payment.setGatewaySignature(request.getRazorpaySignature());
        payment.setStatus(PaymentStatus.HELD);
        paymentRepository.save(payment);

        log.info("Payment held for contract: {}", payment.getContract().getId());
    }

    @Transactional
    public void releasePayment(UUID contractId) {
        Payment payment = paymentRepository.findByContractId(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.HELD) {
            throw new BadRequestException("Payment is not in held state");
        }

        payment.setStatus(PaymentStatus.RELEASED);
        paymentRepository.save(payment);

        log.info("Payment released: ₹{} to student: {}",
                payment.getStudentPayout(), payment.getStudent().getId());
    }

    @Transactional
    public void handleWebhook(String payload, String signature) {
        if (!razorpayClient.verifyWebhookSignature(payload, signature)) {
            throw new UnauthorizedException("Invalid webhook signature");
        }

        // Parse event and handle payment.captured
        // Razorpay sends payment.captured when payment succeeds
        // This is a safety net in case client-side verification is missed
        log.info("Webhook received and verified");
    }

    @Transactional
    public void refundPayment(UUID contractId) {
        Payment payment = paymentRepository.findByContractId(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.HELD
                && payment.getStatus() != PaymentStatus.DISPUTED) {
            throw new BadRequestException("Payment cannot be refunded in current state");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        log.info("Payment refunded for contract: {}", contractId);
    }

    @Transactional(readOnly = true)
    public EarningsResponse getEarnings(UUID studentId) {
        return EarningsResponse.builder()
                .totalEarned(paymentRepository.getTotalEarningsByStudentId(studentId))
                .pendingRelease(paymentRepository.getPendingEarningsByStudentId(studentId))
                .build();
    }

    @Transactional
    public PaymentBillResponse markPayoutSent(UUID adminId, UUID paymentId, PayoutRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.RELEASED) {
            throw new BadRequestException("Payment must be released before marking payout sent");
        }

        if (payment.getPayoutSent()) {
            throw new BadRequestException("Payout already marked as sent");
        }

        payment.setPayoutSent(true);
        payment.setPayoutSentAt(LocalDateTime.now());
        payment.setPayoutNote(request.getPayoutNote());
        paymentRepository.save(payment);

        log.info("Payout marked sent for payment: {} by admin: {}", paymentId, adminId);
        return toBill(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentBillResponse> getPendingPayouts(Pageable pageable) {
        return paymentRepository
                .findByStatusAndPayoutSent(PaymentStatus.RELEASED, false, pageable)
                .map(this::toBill);
    }

    @Transactional(readOnly = true)
    public Page<PaymentBillResponse> getStudentPaymentHistory(UUID studentId, Pageable pageable) {
        return paymentRepository.findByStudentId(studentId, pageable)
                .map(this::toBill);
    }

    @Transactional(readOnly = true)
    public Page<PaymentBillResponse> getClientPaymentHistory(UUID clientId, Pageable pageable) {
        return paymentRepository.findByClientId(clientId, pageable)
                .map(this::toBill);
    }

    private PaymentBillResponse toBill(Payment p) {
        return PaymentBillResponse.builder()
                .paymentId(p.getId())
                .contractId(p.getContract().getId())
                .jobTitle(p.getContract().getJob().getTitle())
                .clientName(p.getClient().getName())
                .studentName(p.getStudent().getName())
                .clientPaid(p.getAmount())
                .razorpayFee(p.getRazorpayFee())
                .netReceived(p.getNetReceived())
                .yourCommission(p.getCommission())
                .studentPayout(p.getStudentPayout())
                .status(p.getStatus())
                .payoutSent(p.getPayoutSent())
                .payoutNote(p.getPayoutNote())
                .payoutSentAt(p.getPayoutSentAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}