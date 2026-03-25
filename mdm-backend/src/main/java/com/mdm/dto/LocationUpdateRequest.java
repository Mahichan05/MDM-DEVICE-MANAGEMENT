package com.mdm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationUpdateRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private Float accuracy;
    private Double altitude;
    private Float speed;
    private String provider;
    private Integer batteryLevel;
}
