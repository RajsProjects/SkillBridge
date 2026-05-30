package com.skillbridge.backend.dispute;

import com.skillbridge.backend.common.enums.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DisputeRepository extends JpaRepository<Dispute, UUID> {

    Optional<Dispute> findByContractId(UUID contractId);

    Page<Dispute> findByStatus(DisputeStatus status, Pageable pageable);

    Page<Dispute> findByOpenedById(UUID userId, Pageable pageable);

    boolean existsByContractId(UUID contractId);
}