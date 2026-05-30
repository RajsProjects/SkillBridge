package com.skillbridge.backend.dispute;

import com.skillbridge.backend.common.enums.ContractStatus;
import com.skillbridge.backend.common.enums.DisputeStatus;
import com.skillbridge.backend.common.enums.PaymentStatus;
import com.skillbridge.backend.common.exception.BadRequestException;
import com.skillbridge.backend.common.exception.ResourceNotFoundException;
import com.skillbridge.backend.common.exception.UnauthorizedException;
import com.skillbridge.backend.contract.Contract;
import com.skillbridge.backend.contract.ContractRepository;
import com.skillbridge.backend.dispute.dto.DisputeRequest;
import com.skillbridge.backend.dispute.dto.DisputeResponse;
import com.skillbridge.backend.dispute.dto.ResolveRequest;
import com.skillbridge.backend.payment.Payment;
import com.skillbridge.backend.payment.PaymentRepository;
import com.skillbridge.backend.user.User;
import com.skillbridge.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Transactional
    public DisputeResponse open(UUID userId, DisputeRequest request) {
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        boolean isClient  = contract.getClient().getId().equals(userId);
        boolean isStudent = contract.getStudent().getId().equals(userId);

        if (!isClient && !isStudent) {
            throw new UnauthorizedException("You are not part of this contract");
        }

        if (contract.getStatus() == ContractStatus.COMPLETED
                || contract.getStatus() == ContractStatus.CANCELLED) {
            throw new BadRequestException("Cannot dispute a completed or cancelled contract");
        }

        if (disputeRepository.existsByContractId(contract.getId())) {
            throw new BadRequestException("A dispute already exists for this contract");
        }

        // Freeze payment
        Payment payment = paymentRepository.findByContractId(contract.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for contract"));

        if (payment.getStatus() != PaymentStatus.HELD) {
            throw new BadRequestException("Payment must be held before raising a dispute");
        }

        payment.setStatus(PaymentStatus.DISPUTED);
        paymentRepository.save(payment);

        User opener = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Dispute dispute = Dispute.builder()
                .contract(contract)
                .openedBy(opener)
                .openedByRole(isClient ? "CLIENT" : "STUDENT")
                .reason(request.getReason())
                .proofUrls(request.getProofUrls())
                .build();

        disputeRepository.save(dispute);
        log.info("Dispute opened for contract: {} by: {}", contract.getId(), userId);

        return toResponse(dispute);
    }

    @Transactional
    public DisputeResponse resolve(UUID adminId, UUID disputeId, ResolveRequest request) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found"));

        if (dispute.getStatus() == DisputeStatus.RESOLVED
                || dispute.getStatus() == DisputeStatus.CLOSED) {
            throw new BadRequestException("Dispute is already resolved");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        Payment payment = paymentRepository
                .findByContractId(dispute.getContract().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Admin decides — release to student or refund to client
        if (request.getReleaseToStudent()) {
            payment.setStatus(PaymentStatus.RELEASED);
            log.info("Dispute resolved: payment released to student");
        } else {
            payment.setStatus(PaymentStatus.REFUNDED);
            log.info("Dispute resolved: payment refunded to client");
        }

        paymentRepository.save(payment);

        dispute.setStatus(DisputeStatus.RESOLVED);
        dispute.setResolution(request.getResolution());
        dispute.setResolvedBy(admin);
        dispute.setResolvedAt(LocalDateTime.now());
        disputeRepository.save(dispute);

        return toResponse(dispute);
    }

    @Transactional
    public DisputeResponse markUnderReview(UUID adminId, UUID disputeId) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found"));

        if (dispute.getStatus() != DisputeStatus.OPEN) {
            throw new BadRequestException("Dispute is not open");
        }

        dispute.setStatus(DisputeStatus.UNDER_REVIEW);
        disputeRepository.save(dispute);
        return toResponse(dispute);
    }

    @Transactional(readOnly = true)
    public Page<DisputeResponse> getAllByStatus(DisputeStatus status, Pageable pageable) {
        return disputeRepository.findByStatus(status, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<DisputeResponse> getMyDisputes(UUID userId, Pageable pageable) {
        return disputeRepository.findByOpenedById(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DisputeResponse getById(UUID disputeId) {
        return toResponse(disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found")));
    }

    private DisputeResponse toResponse(Dispute dispute) {
        return DisputeResponse.builder()
                .id(dispute.getId())
                .contractId(dispute.getContract().getId())
                .openedById(dispute.getOpenedBy().getId())
                .openedByName(dispute.getOpenedBy().getName())
                .openedByRole(dispute.getOpenedByRole())
                .reason(dispute.getReason())
                .proofUrls(dispute.getProofUrls())
                .status(dispute.getStatus())
                .resolution(dispute.getResolution())
                .resolvedById(dispute.getResolvedBy() != null
                        ? dispute.getResolvedBy().getId() : null)
                .resolvedByName(dispute.getResolvedBy() != null
                        ? dispute.getResolvedBy().getName() : null)
                .resolvedAt(dispute.getResolvedAt())
                .createdAt(dispute.getCreatedAt())
                .build();
    }
}