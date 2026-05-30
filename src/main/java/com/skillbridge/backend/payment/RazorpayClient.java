package com.skillbridge.backend.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

@Slf4j
@Component
public class RazorpayClient {

    private final RestClient restClient;
    private final String keySecret;
    private final String webhookSecret;

    public RazorpayClient(
            @Value("${razorpay.key-id}") String keyId,
            @Value("${razorpay.key-secret}") String keySecret,
            @Value("${razorpay.webhook-secret}") String webhookSecret,
            @Value("${razorpay.base-url}") String baseUrl) {

        this.keySecret = keySecret;
        this.webhookSecret = webhookSecret;

        String credentials = Base64.getEncoder()
                .encodeToString((keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Basic " + credentials)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Map<String, Object> createOrder(long amountInPaise, String currency, String receipt) {
        Map<String, Object> body = Map.of(
                "amount", amountInPaise,
                "currency", currency,
                "receipt", receipt
        );

        return restClient.post()
                .uri("/orders")
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public boolean verifyPaymentSignature(String orderId,
                                          String paymentId,
                                          String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            String expected = hmacSha256(payload, keySecret);
            return expected.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }

    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            String expected = hmacSha256(payload, webhookSecret);
            return expected.equals(signature);
        } catch (Exception e) {
            log.error("Webhook signature verification failed", e);
            return false;
        }
    }

    private String hmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}