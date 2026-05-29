package com.skillbridge.backend.user.student.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StudentProfileRequest {

    @Size(max = 200, message = "College name too long")
    private String college;

    @Size(max = 100, message = "City name too long")
    private String city;

    @Size(max = 1000, message = "Bio too long")
    private String bio;

    private List<String> skills;
    private List<String> portfolioLinks;

    private String githubUrl;
    private String linkedinUrl;

    @DecimalMin(value = "0.0", message = "Hourly rate must be positive")
    private BigDecimal hourlyRate;
}