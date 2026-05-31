package com.skillbridge.backend.contract;

import com.skillbridge.backend.common.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ContractRepository extends JpaRepository<Contract, UUID> {

    @Query("SELECT c FROM Contract c JOIN FETCH c.job JOIN FETCH c.client JOIN FETCH c.student WHERE c.client.id = :clientId")
    Page<Contract> findByClientIdFetched(@Param("clientId") UUID clientId, Pageable pageable);

    @Query("SELECT c FROM Contract c JOIN FETCH c.job JOIN FETCH c.client JOIN FETCH c.student WHERE c.student.id = :studentId")
    Page<Contract> findByStudentIdFetched(@Param("studentId") UUID studentId, Pageable pageable);

    Optional<Contract> findByJobIdAndStatus(UUID jobId, ContractStatus status);

    boolean existsByJobIdAndStudentId(UUID jobId, UUID studentId);
}