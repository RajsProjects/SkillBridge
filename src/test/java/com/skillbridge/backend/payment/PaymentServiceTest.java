package com.skillbridge.backend.payment;

import com.skillbridge.backend.common.TestDataFactory;
import com.skillbridge.backend.common.enums.ContractStatus;
import com.skillbridge.backend.common.enums.UserRole;
import com.skillbridge.backend.common.exception.BadRequestException;
import com.skillbridge.backend.contract.Contract;
import com.skillbridge.backend.contract.ContractRepository;
import com.skillbridge.backend.job.Job;
import com.skillbridge.backend.payment.dto.OrderRequest;
import com.skillbridge.backend.payment.dto.OrderResponse;
import com.skillbridge.backend.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock PaymentRepository paymentRepository;
    @Mock ContractRepository contractRepository;
    @Mock RazorpayClient razorpayClient;

    @InjectMocks PaymentService paymentService;

    private Contract buildContract(User client, User student, ContractStatus status) {
        Job job = TestDataFactory.buildJob(client);
        return Contract.builder()
                .id(UUID.randomUUID())
                .job(job)
                .client(client)
                .student(student)
                .amount(new BigDecimal("5000.00"))
                .status(status)
                .build();
    }

    @Test
    void createOrder_calculates_commission_correctly() {
        User client  = TestDataFactory.buildUser(UserRole.CLIENT);
        User student = TestDataFactory.buildUser(UserRole.STUDENT);
        Contract contract = buildContract(client, student, ContractStatus.ACTIVE);

        OrderRequest req = new OrderRequest();
        req.setContractId(contract.getId());

        when(contractRepository.findById(any())).thenReturn(Optional.of(contract));
        when(paymentRepository.findByContractId(any())).thenReturn(Optional.empty());
        when(razorpayClient.createOrder(anyLong(), anyString(), anyString()))
                .thenReturn(Map.of("id", "order_test123"));
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = paymentService.createOrder(client.getId(), req);

        assertThat(response.getRazorpayFee())
                .isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.getNetReceived())
                .isEqualByComparingTo(new BigDecimal("4900.00"));
        assertThat(response.getCommission())
                .isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.getStudentPayout())
                .isEqualByComparingTo(new BigDecimal("4400.00"));
    }

    @Test
    void createOrder_throws_when_contract_not_active() {
        User client  = TestDataFactory.buildUser(UserRole.CLIENT);
        User student = TestDataFactory.buildUser(UserRole.STUDENT);
        Contract contract = buildContract(client, student, ContractStatus.COMPLETED);

        OrderRequest req = new OrderRequest();
        req.setContractId(contract.getId());

        when(contractRepository.findById(any())).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> paymentService.createOrder(client.getId(), req))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Contract is not in active state");
    }

    @Test
    void createOrder_throws_when_payment_already_exists() {
        User client  = TestDataFactory.buildUser(UserRole.CLIENT);
        User student = TestDataFactory.buildUser(UserRole.STUDENT);
        Contract contract = buildContract(client, student, ContractStatus.ACTIVE);

        OrderRequest req = new OrderRequest();
        req.setContractId(contract.getId());

        when(contractRepository.findById(any())).thenReturn(Optional.of(contract));
        when(paymentRepository.findByContractId(any()))
                .thenReturn(Optional.of(new Payment()));

        assertThatThrownBy(() -> paymentService.createOrder(client.getId(), req))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Payment already created for this contract");
    }
}