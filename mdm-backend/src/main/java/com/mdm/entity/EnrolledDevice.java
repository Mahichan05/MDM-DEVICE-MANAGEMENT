package com.mdm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "enrolled_devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrolledDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "device_id", unique = true, nullable = false)
    private String deviceId;

    @Column(name = "enrollment_token")
    private String enrollmentToken;

    @Column(name = "enrollment_method")
    private String enrollmentMethod; // QR_CODE or TOKEN

    @Column(name = "enrolled_at")
    private LocalDateTime enrolledAt;

    @Column(name = "status")
    @Builder.Default
    private String status = "ACTIVE";

    @OneToOne(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private DeviceInfo deviceInfo;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InstalledApp> installedApps = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        enrolledAt = LocalDateTime.now();
    }
}
