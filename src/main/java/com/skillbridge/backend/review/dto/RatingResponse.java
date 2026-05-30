package com.skillbridge.backend.review.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RatingResponse {
    private Double averageRating;
    private long totalReviews;
}