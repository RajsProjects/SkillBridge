package com.skillbridge.backend.dispute;

import com.skillbridge.backend.common.enums.DisputeStatus;
import com.skillbridge.backend.common.responses.ApiResponse;
import com.skillbridge.backend.dispute.dto.DisputeRequest;
import com.skillbridge.backend.dispute.dto.DisputeResponse;
import com.skillbridge.backend.dispute.dto.ResolveRequest;
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
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<DisputeResponse>> open(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DisputeRequest request) {

        UUID userId = UUID.fromString(userDetails.getUsername());
        DisputeResponse response = disputeService.open(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dispute opened", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<Page<DisputeResponse>>> getMyDisputes(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {

        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse
                .success(disputeService.getMyDisputes(userId, pageable)));
    }

    @GetMapping("/{disputeId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'CLIENT', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<ApiResponse<DisputeResponse>> getById(
            @PathVariable UUID disputeId) {

        return ResponseEntity.ok(ApiResponse
                .success(disputeService.getById(disputeId)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<ApiResponse<Page<DisputeResponse>>> getAllByStatus(
            @RequestParam(defaultValue = "OPEN") DisputeStatus status,
            @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse
                .success(disputeService.getAllByStatus(status, pageable)));
    }

    @PatchMapping("/{disputeId}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<ApiResponse<DisputeResponse>> markUnderReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID disputeId) {

        UUID adminId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse
                .success(disputeService.markUnderReview(adminId, disputeId)));
    }

    @PostMapping("/{disputeId}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DisputeResponse>> resolve(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID disputeId,
            @Valid @RequestBody ResolveRequest request) {

        UUID adminId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse
                .success("Dispute resolved", disputeService.resolve(adminId, disputeId, request)));
    }
}