package com.mdm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppInventoryRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotEmpty(message = "App list must not be empty")
    private List<AppDetail> apps;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AppDetail {
        private String appName;

        @NotBlank(message = "Package name is required")
        private String packageName;

        private String versionName;
        private Long versionCode;
        private String installationSource;
        private Boolean isSystemApp;
        private String category;
    }
}
