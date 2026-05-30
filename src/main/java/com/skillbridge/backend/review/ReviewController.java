package com.skillbridge.backend.review;

import com.skillbridge.backend.common.responses.ApiResponse;
import com.skillbridge.backend.review.dto.RatingResponse;
import com.skillbridge.backend.review.dto.ReviewRequest;
import com.skillbridge.backend.review.dto.ReviewResponse;
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
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<ReviewResponse>> submit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReviewRequest request) {

        UUID reviewerId = UUID.fromString(userDetails.getUsername());
        ReviewResponse response = reviewService.submit(reviewerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted", response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviewsForUser(
            @PathVariable UUID userId,
            @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse
                .success(reviewService.getReviewsForUser(userId, pageable)));
    }

    @GetMapping("/user/{userId}/rating")
    public ResponseEntity<ApiResponse<RatingResponse>> getRating(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(ApiResponse
                .success(reviewService.getRating(userId)));
    }
}