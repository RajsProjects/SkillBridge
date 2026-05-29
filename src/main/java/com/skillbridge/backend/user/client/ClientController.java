package com.skillbridge.backend.user.client;

import com.skillbridge.backend.common.responses.ApiResponse;
import com.skillbridge.backend.user.client.dto.ClientProfileRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PutMapping("/profile")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ClientProfile>> upsertProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ClientProfileRequest request) {

        UUID userId = UUID.fromString(userDetails.getUsername());
        ClientProfile profile = clientService.createOrUpdate(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", profile));
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<ClientProfile>> getProfile(
            @PathVariable UUID userId) {

        ClientProfile profile = clientService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}