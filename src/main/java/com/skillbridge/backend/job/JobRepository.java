package com.skillbridge.backend.job;

import com.skillbridge.backend.common.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    Page<Job> findByClientId(UUID clientId, Pageable pageable);

    @Query("""
        SELECT j FROM Job j
        WHERE j.status = 'OPEN'
        AND (:category IS NULL OR j.category = :category)
        AND (:minBudget IS NULL OR j.budget >= :minBudget)
        AND (:maxBudget IS NULL OR j.budget <= :maxBudget)
        AND (:isRemote IS NULL OR j.isRemote = :isRemote)
        AND (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY j.createdAt DESC
    """)
    Page<Job> searchJobs(
            @Param("category") String category,
            @Param("minBudget") BigDecimal minBudget,
            @Param("maxBudget") BigDecimal maxBudget,
            @Param("isRemote") Boolean isRemote,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}