package com.mdm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_commands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @NotBlank
    @Column(name = "command_type", nullable = false)
    private String commandType; // FACTORY_RESET, REBOOT, ALARM, LOCK, STOP_ALARM, UNINSTALL_APP

    @Column(name = "payload")
    private String payload; // e.g. package name for UNINSTALL_APP

    @Column(name = "status")
    @Builder.Default
    private String status = "PENDING"; // PENDING, EXECUTED, FAILED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrolled_device_id")
    private EnrolledDevice device;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
