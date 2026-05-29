package com.skillbridge.backend.application;

import com.skillbridge.backend.application.dto.ApplicationRequest;
import com.skillbridge.backend.application.dto.ApplicationResponse;
import com.skillbridge.backend.common.responses.ApiResponse;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService applicationService;

    @PostMapping("/jobs/{jobId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> apply(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID jobId,
            @Valid @RequestBody ApplicationRequest request) {

        UUID studentId = UUID.fromString(userDetails.getUsername());
        ApplicationResponse response = applicationService.apply(studentId, jobId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted", response));
    }

    @GetMapping("/jobs/{jobId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getJobApplications(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID jobId,
            @PageableDefault() Pageable pageable) {

        UUID clientId = UUID.fromString(userDetails.getUsername());
        Page<ApplicationResponse> applications =
                applicationService.getJobApplications(clientId, jobId, pageable);
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getMyApplications(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault() Pageable pageable) {

        UUID studentId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse
                .success(applicationService.getMyApplications(studentId, pageable)));
    }

    @PatchMapping("/{applicationId}/shortlist")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> shortlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID applicationId) {

        UUID clientId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse
                .success(applicationService.shortlist(clientId, applicationId)));
    }

    @PatchMapping("/{applicationId}/reject")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> reject(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID applicationId) {

        UUID clientId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse
                .success(applicationService.reject(clientId, applicationId)));
    }
}