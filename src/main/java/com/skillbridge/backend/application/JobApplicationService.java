package com.skillbridge.backend.application;

import com.skillbridge.backend.application.dto.ApplicationRequest;
import com.skillbridge.backend.application.dto.ApplicationResponse;
import com.skillbridge.backend.common.enums.ApplicationStatus;
import com.skillbridge.backend.common.enums.JobStatus;
import com.skillbridge.backend.common.enums.UserRole;
import com.skillbridge.backend.common.exception.BadRequestException;
import com.skillbridge.backend.common.exception.ResourceNotFoundException;
import com.skillbridge.backend.common.exception.UnauthorizedException;
import com.skillbridge.backend.job.Job;
import com.skillbridge.backend.job.JobRepository;
import com.skillbridge.backend.user.User;
import com.skillbridge.backend.user.UserRepository;
import com.skillbridge.backend.user.student.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;

    @Transactional
    public ApplicationResponse apply(UUID studentId, UUID jobId, ApplicationRequest request) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (student.getRole() != UserRole.STUDENT) {
            throw new UnauthorizedException("Only students can apply to jobs");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (job.getStatus() != JobStatus.OPEN) {
            throw new BadRequestException("Job is no longer accepting applications");
        }

        if (job.getClient().getId().equals(studentId)) {
            throw new BadRequestException("You cannot apply to your own job");
        }

        if (applicationRepository.existsByJobIdAndStudentId(jobId, studentId)) {
            throw new BadRequestException("You have already applied to this job");
        }

        JobApplication application = JobApplication.builder()
                .job(job)
                .student(student)
                .proposal(request.getProposal())
                .priceQuote(request.getPriceQuote())
                .deliveryDays(request.getDeliveryDays())
                .build();

        return toResponse(applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getJobApplications(UUID clientId, UUID jobId, Pageable pageable) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getClient().getId().equals(clientId)) {
            throw new UnauthorizedException("You don't own this job");
        }

        return applicationRepository.findByJobId(jobId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getMyApplications(UUID studentId, Pageable pageable) {
        return applicationRepository.findByStudentId(studentId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public ApplicationResponse shortlist(UUID clientId, UUID applicationId) {
        return updateStatus(clientId, applicationId, ApplicationStatus.SHORTLISTED);
    }

    @Transactional
    public ApplicationResponse reject(UUID clientId, UUID applicationId) {
        return updateStatus(clientId, applicationId, ApplicationStatus.REJECTED);
    }

    private ApplicationResponse updateStatus(UUID clientId, UUID applicationId,
                                             ApplicationStatus newStatus) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getJob().getClient().getId().equals(clientId)) {
            throw new UnauthorizedException("You don't own this job");
        }

        application.setStatus(newStatus);
        return toResponse(applicationRepository.save(application));
    }

    private ApplicationResponse toResponse(JobApplication app) {
        var studentProfile = studentProfileRepository
                .findByUserId(app.getStudent().getId()).orElse(null);

        return ApplicationResponse.builder()
                .id(app.getId())
                .jobId(app.getJob().getId())
                .jobTitle(app.getJob().getTitle())
                .studentId(app.getStudent().getId())
                .studentName(app.getStudent().getName())
                .studentCollege(studentProfile != null ? studentProfile.getCollege() : null)
                .proposal(app.getProposal())
                .priceQuote(app.getPriceQuote())
                .deliveryDays(app.getDeliveryDays())
                .status(app.getStatus())
                .createdAt(app.getCreatedAt())
                .build();
    }
}