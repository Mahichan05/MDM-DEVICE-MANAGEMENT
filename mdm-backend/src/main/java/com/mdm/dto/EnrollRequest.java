package com.mdm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    private String enrollmentToken;

    @NotBlank(message = "Enrollment method is required")
    private String enrollmentMethod; // QR_CODE or TOKEN
}
