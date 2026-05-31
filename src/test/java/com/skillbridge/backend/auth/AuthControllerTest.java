package com.skillbridge.backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbridge.backend.auth.dto.RegisterRequest;
import com.skillbridge.backend.auth.token.RefreshTokenRepository;
import com.skillbridge.backend.common.TestDataFactory;
import com.skillbridge.backend.common.enums.UserRole;
import com.skillbridge.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void cleanUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }
    @Test
    void register_returns_201_with_tokens() throws Exception {
        RegisterRequest req = TestDataFactory.buildRegisterRequest(UserRole.STUDENT);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    void register_returns_400_when_email_duplicate() throws Exception {
        RegisterRequest req = TestDataFactory.buildRegisterRequest(UserRole.STUDENT);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Second registration with same email
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_returns_200_with_tokens() throws Exception {
        RegisterRequest req = TestDataFactory.buildRegisterRequest(UserRole.STUDENT);

        // Register first
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // Then login
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                TestDataFactory.buildLoginRequest(req.getEmail()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.role").value("STUDENT"));
    }

    @Test
    void login_returns_401_wrong_password() throws Exception {
        RegisterRequest req = TestDataFactory.buildRegisterRequest(UserRole.STUDENT);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        req.setPassword("WrongPass@99");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                TestDataFactory.buildLoginRequest(req.getEmail()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_returns_400_when_password_weak() throws Exception {
        RegisterRequest req = TestDataFactory.buildRegisterRequest(UserRole.STUDENT);
        req.setPassword("weak");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}