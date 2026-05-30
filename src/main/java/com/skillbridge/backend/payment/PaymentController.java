package com.skillbridge.backend.payment;

import com.skillbridge.backend.common.responses.ApiResponse;
import com.skillbridge.backend.payment.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderRequest request) {

        UUID clientId = UUID.fromString(userDetails.getUsername());
        OrderResponse response = paymentService.createOrder(clientId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created", response));
    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Void>> verify(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody VerifyRequest request) {

        UUID clientId = UUID.fromString(userDetails.getUsername());
        paymentService.verifyAndHold(clientId, request);
        return ResponseEntity.ok(ApiResponse.success("Payment verified and held", null));
    }

    @PostMapping("/release/{contractId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Void>> release(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID contractId) {

        UUID clientId = UUID.fromString(userDetails.getUsername());
        paymentService.releasePayment(contractId);
        return ResponseEntity.ok(ApiResponse.success("Payment released to student", null));
    }

    @GetMapping("/earnings")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<EarningsResponse>> getEarnings(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID studentId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse
                .success(paymentService.getEarnings(studentId)));
    }

    // Razorpay calls this — no JWT, verified by webhook signature
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }

    // Admin marks payout as sent after manually paying student
    @PostMapping("/{paymentId}/payout")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentBillResponse>> markPayoutSent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID paymentId,
            @Valid @RequestBody PayoutRequest request) {

        UUID adminId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse
                .success("Payout marked as sent",
                        paymentService.markPayoutSent(adminId, paymentId, request)));
    }

    // Admin sees all pending payouts (released but not yet paid to student)
    @GetMapping("/pending-payouts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PaymentBillResponse>>> getPendingPayouts(
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse
                .success(paymentService.getPendingPayouts(pageable)));
    }

    // Student sees their payment history
    @GetMapping("/history/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Page<PaymentBillResponse>>> getStudentHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {

        UUID studentId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse
                .success(paymentService.getStudentPaymentHistory(studentId, pageable)));
    }

    // Client sees their payment history
    @GetMapping("/history/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Page<PaymentBillResponse>>> getClientHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {

        UUID clientId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse
                .success(paymentService.getClientPaymentHistory(clientId, pageable)));
    }
}