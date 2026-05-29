package com.skillbridge.backend.job;

import com.skillbridge.backend.common.responses.ApiResponse;
import com.skillbridge.backend.job.dto.JobRequest;
import com.skillbridge.backend.job.dto.JobResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<JobResponse>> post(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody JobRequest request) {

        UUID clientId = UUID.fromString(userDetails.getUsername());
        JobResponse response = jobService.post(clientId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job posted", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<JobResponse>>> search(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minBudget,
            @RequestParam(required = false) BigDecimal maxBudget,
            @RequestParam(required = false) Boolean isRemote,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        Page<JobResponse> jobs = jobService.search(
                category, minBudget, maxBudget, isRemote, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JobResponse>> getById(
            @PathVariable UUID jobId) {

        return ResponseEntity.ok(ApiResponse.success(jobService.getById(jobId)));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getMyJobs(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {

        UUID clientId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(jobService.getMyJobs(clientId, pageable)));
    }

    @PatchMapping("/{jobId}/close")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<JobResponse>> close(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID jobId) {

        UUID clientId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(jobService.close(clientId, jobId)));
    }
}