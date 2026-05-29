package com.skillbridge.backend.contract;

import com.skillbridge.backend.application.JobApplication;
import com.skillbridge.backend.application.JobApplicationRepository;
import com.skillbridge.backend.common.enums.ApplicationStatus;
import com.skillbridge.backend.common.enums.ContractStatus;
import com.skillbridge.backend.common.enums.JobStatus;
import com.skillbridge.backend.common.exception.BadRequestException;
import com.skillbridge.backend.common.exception.ResourceNotFoundException;
import com.skillbridge.backend.common.exception.UnauthorizedException;
import com.skillbridge.backend.contract.dto.ContractResponse;
import com.skillbridge.backend.contract.dto.SubmitWorkRequest;
import com.skillbridge.backend.job.Job;
import com.skillbridge.backend.job.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    @Transactional
    public ContractResponse hire(UUID clientId, UUID applicationId) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        Job job = application.getJob();

        if (!job.getClient().getId().equals(clientId)) {
            throw new UnauthorizedException("You don't own this job");
        }

        if (job.getStatus() != JobStatus.OPEN) {
            throw new BadRequestException("Job is no longer open");
        }

        if (application.getStatus() == ApplicationStatus.REJECTED) {
            throw new BadRequestException("Cannot hire a rejected application");
        }

        if (contractRepository.existsByJobIdAndStudentId(
                job.getId(), application.getStudent().getId())) {
            throw new BadRequestException("Student already hired for this job");
        }

        // Mark application as hired
        application.setStatus(ApplicationStatus.HIRED);
        applicationRepository.save(application);

        // Mark job as in progress
        job.setStatus(JobStatus.IN_PROGRESS);
        jobRepository.save(job);

        // Create contract
        Contract contract = Contract.builder()
                .job(job)
                .client(job.getClient())
                .student(application.getStudent())
                .amount(application.getPriceQuote())
                .build();

        return toResponse(contractRepository.save(contract));
    }

    @Transactional
    public ContractResponse submitWork(UUID studentId, UUID contractId,
                                       SubmitWorkRequest request) {
        Contract contract = findAndValidate(contractId);

        if (!contract.getStudent().getId().equals(studentId)) {
            throw new UnauthorizedException("This is not your contract");
        }

        if (contract.getStatus() != ContractStatus.ACTIVE
                && contract.getStatus() != ContractStatus.REVISION) {
            throw new BadRequestException("Contract is not in a submittable state");
        }

        contract.setSubmissionUrl(request.getSubmissionUrl());
        contract.setStatus(ContractStatus.SUBMITTED);
        return toResponse(contractRepository.save(contract));
    }

    @Transactional
    public ContractResponse requestRevision(UUID clientId, UUID contractId) {
        Contract contract = findAndValidate(contractId);

        if (!contract.getClient().getId().equals(clientId)) {
            throw new UnauthorizedException("You don't own this contract");
        }

        if (contract.getStatus() != ContractStatus.SUBMITTED) {
            throw new BadRequestException("Work has not been submitted yet");
        }

        contract.setStatus(ContractStatus.REVISION);
        return toResponse(contractRepository.save(contract));
    }

    @Transactional
    public ContractResponse approve(UUID clientId, UUID contractId) {
        Contract contract = findAndValidate(contractId);

        if (!contract.getClient().getId().equals(clientId)) {
            throw new UnauthorizedException("You don't own this contract");
        }

        if (contract.getStatus() != ContractStatus.SUBMITTED) {
            throw new BadRequestException("Work has not been submitted yet");
        }

        contract.setStatus(ContractStatus.COMPLETED);
        contract.setCompletedAt(LocalDateTime.now());
        return toResponse(contractRepository.save(contract));
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getMyContractsAsClient(UUID clientId, Pageable pageable) {
        return contractRepository.findByClientId(clientId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getMyContractsAsStudent(UUID studentId, Pageable pageable) {
        return contractRepository.findByStudentId(studentId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ContractResponse getById(UUID userId, UUID contractId) {
        Contract contract = findAndValidate(contractId);

        boolean isParty = contract.getClient().getId().equals(userId)
                || contract.getStudent().getId().equals(userId);

        if (!isParty) {
            throw new UnauthorizedException("You are not part of this contract");
        }

        return toResponse(contract);
    }

    private Contract findAndValidate(UUID contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
    }

    private ContractResponse toResponse(Contract contract) {
        return ContractResponse.builder()
                .id(contract.getId())
                .jobId(contract.getJob().getId())
                .jobTitle(contract.getJob().getTitle())
                .clientId(contract.getClient().getId())
                .clientName(contract.getClient().getName())
                .studentId(contract.getStudent().getId())
                .studentName(contract.getStudent().getName())
                .amount(contract.getAmount())
                .status(contract.getStatus())
                .submissionUrl(contract.getSubmissionUrl())
                .startedAt(contract.getStartedAt())
                .completedAt(contract.getCompletedAt())
                .build();
    }
}