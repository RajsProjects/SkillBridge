package com.skillbridge.backend.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderRequest {

    @NotNull(message = "Contract ID is required")
    private UUID contractId;
}