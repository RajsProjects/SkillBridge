package com.skillbridge.backend.user.student.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class StudentProfileResponse {
    private UUID userId;
    private String name;
    private String email;
    private String college;
    private String city;
    private String bio;
    private List<String> skills;
    private List<String> portfolioLinks;
    private String githubUrl;
    private String linkedinUrl;
    private BigDecimal hourlyRate;
    private String profileImage;
}