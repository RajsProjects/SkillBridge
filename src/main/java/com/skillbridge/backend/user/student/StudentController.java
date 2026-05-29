package com.skillbridge.backend.user.student;

import com.skillbridge.backend.common.responses.ApiResponse;
import com.skillbridge.backend.user.student.dto.StudentProfileRequest;
import com.skillbridge.backend.user.student.dto.StudentProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PutMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> upsertProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StudentProfileRequest request) {

        UUID userId = UUID.fromString(userDetails.getUsername());
        StudentProfileResponse response = studentService.createOrUpdate(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", response));
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> getProfile(
            @PathVariable UUID userId) {

        StudentProfileResponse response = studentService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}