package com.mdm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "device_model")
    private String deviceModel;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "os_version")
    private String osVersion;

    @Column(name = "sdk_version")
    private Integer sdkVersion;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "unique_identifier")
    private String uniqueIdentifier;

    @Column(name = "imei")
    private String imei;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrolled_device_id")
    private EnrolledDevice device;

    @PrePersist
    protected void onCreate() {
        collectedAt = LocalDateTime.now();
    }
}
