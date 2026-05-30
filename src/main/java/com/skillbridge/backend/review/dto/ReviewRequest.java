package com.skillbridge.backend.review.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class ReviewRequest {

    @NotNull(message = "Contract ID is required")
    private UUID contractId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Short rating;

    @Size(max = 1000, message = "Comment too long")
    private String comment;
}