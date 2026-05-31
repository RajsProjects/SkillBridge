package com.skillbridge.backend.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class RazorpayClientTest {

    private RazorpayClient razorpayClient;

    @BeforeEach
    void setUp() {
        razorpayClient = new RazorpayClient(
                "rzp_test_key",
                "test_secret",
                "test_webhook_secret",
                "https://api.razorpay.com/v1"
        );
    }

    @Test
    void verifyPaymentSignature_returns_false_for_tampered_data() {
        boolean result = razorpayClient.verifyPaymentSignature(
                "order_fake123",
                "pay_fake456",
                "invalidsignature"
        );
        assertThat(result).isFalse();
    }

    @Test
    void verifyWebhookSignature_returns_false_for_invalid_signature() {
        boolean result = razorpayClient.verifyWebhookSignature(
                "{\"event\":\"payment.captured\"}",
                "invalidsignature"
        );
        assertThat(result).isFalse();
    }

    @Test
    void verifyPaymentSignature_returns_true_for_valid_signature() throws Exception {
        // Generate a real HMAC-SHA256 signature the same way Razorpay does
        String orderId   = "order_test123";
        String paymentId = "pay_test456";
        String payload   = orderId + "|" + paymentId;

        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(
                "test_secret".getBytes(), "HmacSHA256"));
        String validSignature = java.util.HexFormat.of().formatHex(
                mac.doFinal(payload.getBytes()));

        boolean result = razorpayClient.verifyPaymentSignature(
                orderId, paymentId, validSignature);

        assertThat(result).isTrue();
    }
}