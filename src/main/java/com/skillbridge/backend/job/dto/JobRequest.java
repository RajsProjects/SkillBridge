package com.skillbridge.backend.job.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class JobRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title too long")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String category;
    private List<String> skillsRequired;

    @NotNull(message = "Budget is required")
    @DecimalMin(value = "1.0", message = "Budget must be at least ₹1")
    private BigDecimal budget;

    private LocalDate deadline;

    private Boolean isRemote = true;
}