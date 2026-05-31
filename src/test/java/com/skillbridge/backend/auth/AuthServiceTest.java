package com.skillbridge.backend.auth;

import com.skillbridge.backend.auth.dto.LoginRequest;
import com.skillbridge.backend.auth.dto.RegisterRequest;
import com.skillbridge.backend.auth.dto.AuthResponse;
import com.skillbridge.backend.auth.token.RefreshTokenRepository;
import com.skillbridge.backend.common.TestDataFactory;
import com.skillbridge.backend.common.enums.UserRole;
import com.skillbridge.backend.common.exception.BadRequestException;
import com.skillbridge.backend.common.exception.UnauthorizedException;
import com.skillbridge.backend.security.JwtUtil;
import com.skillbridge.backend.user.User;
import com.skillbridge.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;

    @InjectMocks AuthService authService;

    private User student;

    @BeforeEach
    void setUp() {
        student = TestDataFactory.buildUser(UserRole.STUDENT);
    }

    @Test
    void register_success() {
        RegisterRequest req = TestDataFactory.buildRegisterRequest(UserRole.STUDENT);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(student);
        when(jwtUtil.generateAccessToken(any(), any(), any())).thenReturn("access");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh");
        when(refreshTokenRepository.save(any())).thenReturn(null);

        AuthResponse response = authService.register(req);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access");
        verify(userRepository).save(any());
    }

    @Test
    void register_throws_when_email_exists() {
        RegisterRequest req = TestDataFactory.buildRegisterRequest(UserRole.STUDENT);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email already registered");
    }

    @Test
    void login_success() {
        LoginRequest req = TestDataFactory.buildLoginRequest(student.getEmail());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(student));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateAccessToken(any(), any(), any())).thenReturn("access");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh");
        when(refreshTokenRepository.save(any())).thenReturn(null);

        AuthResponse response = authService.login(req);

        assertThat(response.getEmail()).isEqualTo(student.getEmail());
    }

    @Test
    void login_throws_when_wrong_password() {
        LoginRequest req = TestDataFactory.buildLoginRequest(student.getEmail());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(student));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_throws_when_user_banned() {
        student.setActive(false);
        LoginRequest req = TestDataFactory.buildLoginRequest(student.getEmail());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Account is deactivated");
    }

    @Test
    void login_throws_when_user_not_found() {
        LoginRequest req = TestDataFactory.buildLoginRequest("ghost@test.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(UnauthorizedException.class);
    }
}