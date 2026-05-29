package com.skillbridge.backend.contract;

import com.skillbridge.backend.common.responses.ApiResponse;
import com.skillbridge.backend.contract.dto.ContractResponse;
import com.skillbridge.backend.contract.dto.SubmitWorkRequest;
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
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping("/hire/{applicationId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ContractResponse>> hire(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID applicationId) {

        UUID clientId = UUID.fromString(userDetails.getUsername());
        ContractResponse response = contractService.hire(clientId, applicationId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student hired successfully", response));
    }

    @PatchMapping("/{contractId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ContractResponse>> submitWork(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID contractId,
            @Valid @RequestBody SubmitWorkRequest request) {

        UUID studentId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse
                .success("Work submitted", contractService.submitWork(studentId, contractId, request)));
    }

    @PatchMapping("/{contractId}/revision")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ContractResponse>> requestRevision(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID contractId) {

        UUID clientId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse
                .success("Revision requested", contractService.requestRevision(clientId, contractId)));
    }

    @PatchMapping("/{contractId}/approve")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ContractResponse>> approve(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID contractId) {

        UUID clientId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse
                .success("Work approved", contractService.approve(clientId, contractId)));
    }

    @GetMapping("/my/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Page<ContractResponse>>> getMyContractsAsClient(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {

        UUID clientId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse
                .success(contractService.getMyContractsAsClient(clientId, pageable)));
    }

    @GetMapping("/my/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Page<ContractResponse>>> getMyContractsAsStudent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {

        UUID studentId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse
                .success(contractService.getMyContractsAsStudent(studentId, pageable)));
    }

    @GetMapping("/{contractId}")
    public ResponseEntity<ApiResponse<ContractResponse>> getById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID contractId) {

        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse
                .success(contractService.getById(userId, contractId)));
    }
}