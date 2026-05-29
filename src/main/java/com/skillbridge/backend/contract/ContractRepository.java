package com.skillbridge.backend.contract;

import com.skillbridge.backend.common.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ContractRepository extends JpaRepository<Contract, UUID> {

    Page<Contract> findByClientId(UUID clientId, Pageable pageable);

    Page<Contract> findByStudentId(UUID studentId, Pageable pageable);

    Optional<Contract> findByJobIdAndStatus(UUID jobId, ContractStatus status);

    boolean existsByJobIdAndStudentId(UUID jobId, UUID studentId);
}