package com.mdm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceInfoRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    private String deviceModel;
    private String manufacturer;
    private String osVersion;
    private Integer sdkVersion;
    private String serialNumber;
    private String uniqueIdentifier;
    private String imei;
    private String deviceType;
}
