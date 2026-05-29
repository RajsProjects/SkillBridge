package com.skillbridge.backend.user.client.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientProfileRequest {

    @Size(max = 200)
    private String businessName;

    @Size(max = 100)
    private String category;

    @Size(max = 100)
    private String city;

    @Size(max = 20)
    private String contactNumber;

    private String website;
}