package com.skillbridge.backend.application;

import com.skillbridge.backend.common.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    Page<JobApplication> findByJobId(UUID jobId, Pageable pageable);

    Page<JobApplication> findByStudentId(UUID studentId, Pageable pageable);

    Optional<JobApplication> findByJobIdAndStudentId(UUID jobId, UUID studentId);

    boolean existsByJobIdAndStudentId(UUID jobId, UUID studentId);

    long countByJobIdAndStatus(UUID jobId, ApplicationStatus status);
}