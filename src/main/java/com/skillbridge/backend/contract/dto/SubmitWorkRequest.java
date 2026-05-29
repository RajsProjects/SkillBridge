package com.skillbridge.backend.contract.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitWorkRequest {

    @NotBlank(message = "Submission URL is required")
    private String submissionUrl;
}