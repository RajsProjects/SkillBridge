package com.skillbridge.backend.job;

import com.skillbridge.backend.common.enums.JobStatus;
import com.skillbridge.backend.common.enums.UserRole;
import com.skillbridge.backend.common.exception.BadRequestException;
import com.skillbridge.backend.common.exception.ResourceNotFoundException;
import com.skillbridge.backend.common.exception.UnauthorizedException;
import com.skillbridge.backend.job.dto.JobRequest;
import com.skillbridge.backend.job.dto.JobResponse;
import com.skillbridge.backend.user.User;
import com.skillbridge.backend.user.UserRepository;
import com.skillbridge.backend.user.client.ClientProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;

    @Transactional
    public JobResponse post(UUID clientId, JobRequest request) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (client.getRole() != UserRole.CLIENT) {
            throw new UnauthorizedException("Only clients can post jobs");
        }

        Job job = Job.builder()
                .client(client)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .skillsRequired(request.getSkillsRequired())
                .budget(request.getBudget())
                .deadline(request.getDeadline())
                .isRemote(request.getIsRemote())
                .build();

        return toResponse(jobRepository.save(job));
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> search(String category,
                                    BigDecimal minBudget,
                                    BigDecimal maxBudget,
                                    Boolean isRemote,
                                    String keyword,
                                    Pageable pageable) {
        return jobRepository
                .searchJobs(category, minBudget, maxBudget, isRemote, keyword, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public JobResponse getById(UUID jobId) {
        return toResponse(jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found")));
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getMyJobs(UUID clientId, Pageable pageable) {
        return jobRepository.findByClientId(clientId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public JobResponse close(UUID clientId, UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getClient().getId().equals(clientId)) {
            throw new UnauthorizedException("You don't own this job");
        }

        if (job.getStatus() != JobStatus.OPEN) {
            throw new BadRequestException("Job is not open");
        }

        job.setStatus(JobStatus.CANCELLED);
        return toResponse(jobRepository.save(job));
    }

    private JobResponse toResponse(Job job) {
        var clientProfile = clientProfileRepository
                .findByUserId(job.getClient().getId()).orElse(null);

        return JobResponse.builder()
                .id(job.getId())
                .clientId(job.getClient().getId())
                .clientName(job.getClient().getName())
                .clientBusiness(clientProfile != null ? clientProfile.getBusinessName() : null)
                .title(job.getTitle())
                .description(job.getDescription())
                .category(job.getCategory())
                .skillsRequired(job.getSkillsRequired())
                .budget(job.getBudget())
                .deadline(job.getDeadline())
                .isRemote(job.getIsRemote())
                .status(job.getStatus())
                .createdAt(job.getCreatedAt())
                .build();
    }
}