package com.skillbridge.backend.payment;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.skillbridge.backend.auth.AuthService;
import com.skillbridge.backend.auth.dto.AuthResponse;
import com.skillbridge.backend.auth.dto.RegisterRequest;
import com.skillbridge.backend.auth.token.RefreshTokenRepository;
import com.skillbridge.backend.common.enums.ContractStatus;
import com.skillbridge.backend.common.enums.UserRole;
import com.skillbridge.backend.contract.Contract;
import com.skillbridge.backend.contract.ContractRepository;
import com.skillbridge.backend.job.Job;
import com.skillbridge.backend.job.JobRepository;
import com.skillbridge.backend.user.User;
import com.skillbridge.backend.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AuthService authService;
    @Autowired
    static UserRepository userRepository;
    @Autowired JobRepository jobRepository;
    @Autowired ContractRepository contractRepository;
    @Autowired
    static RefreshTokenRepository refreshTokenRepository;

    static WireMockServer wireMock;

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(wireMockConfig().port(8089));
        wireMock.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        // Use WireMock class directly to avoid import conflict with MockMvc's post()
        com.github.tomakehurst.wiremock.client.WireMock.stubFor(
                com.github.tomakehurst.wiremock.client.WireMock.post(
                                com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo("/orders"))
                        .willReturn(com.github.tomakehurst.wiremock.client.WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("""
                    {
                      "id": "order_test123",
                      "amount": 500000,
                      "currency": "INR",
                      "status": "created"
                    }
                """)));
    }

    @Test
    void createOrder_returns_201_with_breakdown() throws Exception {
        // Register client
        RegisterRequest clientReq = new RegisterRequest();
        clientReq.setName("Test Client");
        clientReq.setEmail("client_" + UUID.randomUUID() + "@test.com");
        clientReq.setPassword("Test@1234");
        clientReq.setRole(UserRole.CLIENT);
        AuthResponse clientAuth = authService.register(clientReq);

        // Register student
        RegisterRequest studentReq = new RegisterRequest();
        studentReq.setName("Test Student");
        studentReq.setEmail("student_" + UUID.randomUUID() + "@test.com");
        studentReq.setPassword("Test@1234");
        studentReq.setRole(UserRole.STUDENT);
        AuthResponse studentAuth = authService.register(studentReq);

        User client  = userRepository.findByEmail(clientReq.getEmail()).orElseThrow();
        User student = userRepository.findByEmail(studentReq.getEmail()).orElseThrow();

        // Create job
        Job job = Job.builder()
                .client(client)
                .title("Test Job")
                .description("Test description for job")
                .budget(new BigDecimal("5000.00"))
                .isRemote(true)
                .build();
        jobRepository.save(job);

        // Create contract
        Contract contract = Contract.builder()
                .job(job)
                .client(client)
                .student(student)
                .amount(new BigDecimal("5000.00"))
                .status(ContractStatus.ACTIVE)
                .build();
        contractRepository.save(contract);

        // Create payment order
        Map<String, Object> body = Map.of("contractId", contract.getId().toString());

        mockMvc.perform(post("/api/payments/create-order")
                        .header("Authorization", "Bearer " + clientAuth.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.razorpayOrderId").value("order_test123"))
                .andExpect(jsonPath("$.data.amount").value(5000.00))
                .andExpect(jsonPath("$.data.razorpayFee").value(100.00))
                .andExpect(jsonPath("$.data.commission").value(500.00))
                .andExpect(jsonPath("$.data.studentPayout").value(4400.00));
    }

    @Test
    void verify_payment_fails_with_invalid_signature() throws Exception {
        RegisterRequest clientReq = new RegisterRequest();
        clientReq.setName("Test Client");
        clientReq.setEmail("client_" + UUID.randomUUID() + "@test.com");
        clientReq.setPassword("Test@1234");
        clientReq.setRole(UserRole.CLIENT);
        AuthResponse clientAuth = authService.register(clientReq);

        Map<String, String> body = Map.of(
                "razorpayOrderId",   "order_fake",
                "razorpayPaymentId", "pay_fake",
                "razorpaySignature", "invalidsignature"
        );

        mockMvc.perform(post("/api/payments/verify")
                        .header("Authorization", "Bearer " + clientAuth.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }
}