package com.skillbridge.backend.review.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewResponse {
    private UUID id;
    private UUID contractId;
    private UUID reviewerId;
    private String reviewerName;
    private UUID reviewedUserId;
    private String reviewedUserName;
    private Short rating;
    private String comment;
    private LocalDateTime createdAt;
}