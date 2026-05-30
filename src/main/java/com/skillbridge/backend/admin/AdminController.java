package com.skillbridge.backend.admin;

import com.skillbridge.backend.admin.dto.DashboardResponse;
import com.skillbridge.backend.admin.dto.UserSummary;
import com.skillbridge.backend.common.enums.UserRole;
import com.skillbridge.backend.common.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse
                .success(adminService.getDashboard()));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserSummary>>> getAllUsers(
            @RequestParam(required = false) UserRole role,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<UserSummary> users = role != null
                ? adminService.getUsersByRole(role, pageable)
                : adminService.getAllUsers(pageable);

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PatchMapping("/users/{userId}/ban")
    public ResponseEntity<ApiResponse<UserSummary>> banUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID userId) {

        UUID adminId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse
                .success("User banned", adminService.banUser(adminId, userId)));
    }

    @PatchMapping("/users/{userId}/unban")
    public ResponseEntity<ApiResponse<UserSummary>> unbanUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID userId) {

        UUID adminId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse
                .success("User unbanned", adminService.unbanUser(adminId, userId)));
    }

    @PatchMapping("/users/{userId}/verify")
    public ResponseEntity<ApiResponse<UserSummary>> verifyUser(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(ApiResponse
                .success("User verified", adminService.verifyUser(userId)));
    }
}