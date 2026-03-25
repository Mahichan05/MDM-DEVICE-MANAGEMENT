package com.mdm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceCommandRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "Command type is required")
    private String commandType; // FACTORY_RESET, REBOOT, ALARM, LOCK, STOP_ALARM, UNINSTALL_APP

    private String payload; // e.g. package name for UNINSTALL_APP
}
