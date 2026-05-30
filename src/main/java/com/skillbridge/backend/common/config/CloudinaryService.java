package com.skillbridge.backend.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryService {

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final RestClient restClient;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {

        this.cloudName = cloudName;
        this.apiKey    = apiKey;
        this.apiSecret = apiSecret;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.cloudinary.com/v1_1/" + cloudName)
                .build();
    }

    public String uploadImage(MultipartFile file, String folder) {
        try {
            String timestamp  = String.valueOf(System.currentTimeMillis() / 1000);
            String toSign     = "folder=" + folder + "&timestamp=" + timestamp + apiSecret;
            String signature  = sha1Hex(toSign);

            String base64File = "data:" + file.getContentType() + ";base64,"
                    + Base64.getEncoder().encodeToString(file.getBytes());

            Map<String, Object> body = Map.of(
                    "file",      base64File,
                    "folder",    folder,
                    "timestamp", timestamp,
                    "api_key",   apiKey,
                    "signature", signature
            );

            Map<String, Object> response = restClient.post()
                    .uri("/image/upload")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            assert response != null;
            return (String) response.get("secure_url");

        } catch (Exception e) {
            log.error("Cloudinary upload failed", e);
            throw new RuntimeException("File upload failed");
        }
    }

    private String sha1Hex(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(
                apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        return HexFormat.of().formatHex(
                mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}