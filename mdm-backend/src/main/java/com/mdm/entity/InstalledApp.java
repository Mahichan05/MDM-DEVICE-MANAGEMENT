package com.mdm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "installed_apps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstalledApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "app_name")
    private String appName;

    @NotBlank
    @Column(name = "package_name", nullable = false)
    private String packageName;

    @Column(name = "version_name")
    private String versionName;

    @Column(name = "version_code")
    private Long versionCode;

    @Column(name = "installation_source")
    private String installationSource;

    @Column(name = "is_system_app")
    private Boolean isSystemApp;

    @Column(name = "category")
    private String category;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrolled_device_id")
    private EnrolledDevice device;

    @PrePersist
    protected void onCreate() {
        collectedAt = LocalDateTime.now();
    }
}
