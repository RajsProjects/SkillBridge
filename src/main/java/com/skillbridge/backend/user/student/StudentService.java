package com.skillbridge.backend.user.student;

import com.skillbridge.backend.common.exception.BadRequestException;
import com.skillbridge.backend.common.exception.ResourceNotFoundException;
import com.skillbridge.backend.common.exception.UnauthorizedException;
import com.skillbridge.backend.common.enums.UserRole;
import com.skillbridge.backend.user.User;
import com.skillbridge.backend.user.UserRepository;
import com.skillbridge.backend.user.student.dto.StudentProfileRequest;
import com.skillbridge.backend.user.student.dto.StudentProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public StudentProfileResponse createOrUpdate(UUID userId, StudentProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != UserRole.STUDENT) {
            throw new UnauthorizedException("Only students can create a student profile");
        }

        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElse(StudentProfile.builder().user(user).build());

        profile.setCollege(request.getCollege());
        profile.setCity(request.getCity());
        profile.setBio(request.getBio());
        profile.setSkills(request.getSkills());
        profile.setPortfolioLinks(request.getPortfolioLinks());
        profile.setGithubUrl(request.getGithubUrl());
        profile.setLinkedinUrl(request.getLinkedinUrl());
        profile.setHourlyRate(request.getHourlyRate());

        studentProfileRepository.save(profile);
        return toResponse(user, profile);
    }

    @Transactional(readOnly = true)
    public StudentProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        return toResponse(user, profile);
    }

    private StudentProfileResponse toResponse(User user, StudentProfile profile) {
        return StudentProfileResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .college(profile.getCollege())
                .city(profile.getCity())
                .bio(profile.getBio())
                .skills(profile.getSkills())
                .portfolioLinks(profile.getPortfolioLinks())
                .githubUrl(profile.getGithubUrl())
                .linkedinUrl(profile.getLinkedinUrl())
                .hourlyRate(profile.getHourlyRate())
                .profileImage(profile.getProfileImage())
                .build();
    }
}