package com.skillbridge.backend.admin;

import com.skillbridge.backend.common.enums.JobStatus;
import com.skillbridge.backend.common.enums.UserRole;
import com.skillbridge.backend.common.enums.ContractStatus;
import com.skillbridge.backend.common.enums.DisputeStatus;
import com.skillbridge.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<User, UUID> {

    long countByRole(UserRole role);

    long countByActiveTrue();

    @Query("SELECT COUNT(j) FROM Job j WHERE j.status = :status")
    long countJobsByStatus(@Param("status") JobStatus status);

    @Query("SELECT COUNT(j) FROM Job j")
    long countAllJobs();

    @Query("SELECT COUNT(c) FROM Contract c WHERE c.status = :status")
    long countContractsByStatus(@Param("status") ContractStatus status);

    @Query("SELECT COUNT(c) FROM Contract c")
    long countAllContracts();

    @Query("SELECT COUNT(d) FROM Dispute d WHERE d.status = :status")
    long countDisputesByStatus(@Param("status") DisputeStatus status);

    @Query("SELECT COUNT(d) FROM Dispute d")
    long countAllDisputes();

    @Query("""
        SELECT COALESCE(SUM(p.commission), 0)
        FROM Payment p
        WHERE p.status = 'RELEASED'
    """)
    BigDecimal getTotalRevenue();
}