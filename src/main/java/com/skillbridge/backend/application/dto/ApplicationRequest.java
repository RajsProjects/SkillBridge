package com.skillbridge.backend.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplicationRequest {

    @NotBlank(message = "Proposal is required")
    @Size(min = 50, message = "Proposal must be at least 50 characters")
    private String proposal;

    @NotNull(message = "Price quote is required")
    @DecimalMin(value = "1.0", message = "Price must be at least ₹1")
    private BigDecimal priceQuote;

    @NotNull(message = "Delivery days is required")
    @Min(value = 1, message = "Delivery must be at least 1 day")
    @Max(value = 365, message = "Delivery cannot exceed 365 days")
    private Integer deliveryDays;
}