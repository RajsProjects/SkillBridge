package com.skillbridge.backend.common;

import com.skillbridge.backend.auth.dto.LoginRequest;
import com.skillbridge.backend.auth.dto.RegisterRequest;
import com.skillbridge.backend.common.enums.UserRole;
import com.skillbridge.backend.job.Job;
import com.skillbridge.backend.user.User;

import java.math.BigDecimal;
import java.util.UUID;

public class TestDataFactory {

    public static User buildUser(UserRole role) {
        return User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email("test_" + UUID.randomUUID() + "@skillbridge.com")
                .password("$2a$12$hashedpassword")
                .role(role)
                .verified(false)
                .active(true)
                .build();
    }

    public static RegisterRequest buildRegisterRequest(UserRole role) {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test User");
        req.setEmail("test_" + UUID.randomUUID() + "@skillbridge.com");
        req.setPassword("Test@1234");
        req.setRole(role);
        return req;
    }

    public static LoginRequest buildLoginRequest(String email) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword("Test@1234");
        return req;
    }

    public static Job buildJob(User client) {
        return Job.builder()
                .id(UUID.randomUUID())
                .client(client)
                .title("Build Android App")
                .description("Need a student to build a basic Android app")
                .category("Mobile Development")
                .budget(new BigDecimal("5000.00"))
                .isRemote(true)
                .build();
    }
}