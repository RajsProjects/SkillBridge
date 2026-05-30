package com.skillbridge.backend.admin;

import com.skillbridge.backend.admin.dto.DashboardResponse;
import com.skillbridge.backend.admin.dto.UserSummary;
import com.skillbridge.backend.common.enums.*;
import com.skillbridge.backend.common.exception.BadRequestException;
import com.skillbridge.backend.common.exception.ResourceNotFoundException;
import com.skillbridge.backend.user.User;
import com.skillbridge.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        return DashboardResponse.builder()
                .totalUsers(adminRepository.count())
                .totalStudents(adminRepository.countByRole(UserRole.STUDENT))
                .totalClients(adminRepository.countByRole(UserRole.CLIENT))
                .totalJobs(adminRepository.countAllJobs())
                .openJobs(adminRepository.countJobsByStatus(JobStatus.OPEN))
                .totalContracts(adminRepository.countAllContracts())
                .activeContracts(adminRepository.countContractsByStatus(ContractStatus.ACTIVE))
                .completedContracts(adminRepository.countContractsByStatus(ContractStatus.COMPLETED))
                .totalDisputes(adminRepository.countAllDisputes())
                .openDisputes(adminRepository.countDisputesByStatus(DisputeStatus.OPEN))
                .totalRevenue(adminRepository.getTotalRevenue())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<UserSummary> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<UserSummary> getUsersByRole(UserRole role, Pageable pageable) {
        return userRepository.findByRole(role, pageable)
                .map(this::toSummary);
    }

    @Transactional
    public UserSummary banUser(UUID adminId, UUID userId) {
        if (adminId.equals(userId)) {
            throw new BadRequestException("You cannot ban yourself");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getActive()) {
            throw new BadRequestException("User is already banned");
        }

        if (user.getRole() == UserRole.ADMIN) {
            throw new BadRequestException("Cannot ban another admin");
        }

        user.setActive(false);
        userRepository.save(user);
        log.info("User banned: {} by admin: {}", userId, adminId);
        return toSummary(user);
    }

    @Transactional
    public UserSummary unbanUser(UUID adminId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getActive()) {
            throw new BadRequestException("User is not banned");
        }

        user.setActive(true);
        userRepository.save(user);
        log.info("User unbanned: {} by admin: {}", userId, adminId);
        return toSummary(user);
    }

    @Transactional
    public UserSummary verifyUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setVerified(true);
        userRepository.save(user);
        log.info("User verified: {}", userId);
        return toSummary(user);
    }

    private UserSummary toSummary(User user) {
        return UserSummary.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .verified(user.getVerified())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}