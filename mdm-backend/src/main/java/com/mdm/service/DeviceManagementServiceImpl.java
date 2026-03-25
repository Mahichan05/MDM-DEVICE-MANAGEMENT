package com.mdm.service;

import com.mdm.dto.AppInventoryRequest;
import com.mdm.dto.DeviceInfoRequest;
import com.mdm.dto.DeviceCommandRequest;
import com.mdm.dto.EnrollRequest;
import com.mdm.dto.LocationUpdateRequest;
import com.mdm.entity.DeviceInfo;
import com.mdm.entity.DeviceCommand;
import com.mdm.entity.DeviceLocation;
import com.mdm.entity.EnrolledDevice;
import com.mdm.entity.InstalledApp;
import com.mdm.repository.DeviceInfoRepository;
import com.mdm.repository.DeviceCommandRepository;
import com.mdm.repository.DeviceLocationRepository;
import com.mdm.repository.EnrolledDeviceRepository;
import com.mdm.repository.InstalledAppRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceManagementServiceImpl implements DeviceManagementService {

    private final EnrolledDeviceRepository enrolledDeviceRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final InstalledAppRepository installedAppRepository;
    private final DeviceCommandRepository deviceCommandRepository;
    private final DeviceLocationRepository deviceLocationRepository;

    @Override
    @Transactional
    public EnrolledDevice enrollDevice(EnrollRequest request) {
        log.info("Enrolling device: {}", request.getDeviceId());

        // Check if device is already enrolled
        if (enrolledDeviceRepository.existsByDeviceId(request.getDeviceId())) {
            log.warn("Device {} is already enrolled, updating enrollment", request.getDeviceId());
            EnrolledDevice existing = enrolledDeviceRepository.findByDeviceId(request.getDeviceId())
                    .orElseThrow();
            existing.setEnrollmentToken(request.getEnrollmentToken());
            existing.setEnrollmentMethod(request.getEnrollmentMethod());
            existing.setStatus("ACTIVE");
            return enrolledDeviceRepository.save(existing);
        }

        EnrolledDevice device = EnrolledDevice.builder()
                .deviceId(request.getDeviceId())
                .enrollmentToken(request.getEnrollmentToken())
                .enrollmentMethod(request.getEnrollmentMethod())
                .status("ACTIVE")
                .build();

        return enrolledDeviceRepository.save(device);
    }

    @Override
    @Transactional
    public DeviceInfo saveDeviceInfo(DeviceInfoRequest request) {
        log.info("Saving device info for: {}", request.getDeviceId());

        EnrolledDevice enrolledDevice = enrolledDeviceRepository.findByDeviceId(request.getDeviceId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Device not enrolled: " + request.getDeviceId()));

        // Update existing or create new
        DeviceInfo deviceInfo = deviceInfoRepository.findByDeviceId(request.getDeviceId())
                .orElse(new DeviceInfo());

        deviceInfo.setDeviceId(request.getDeviceId());
        deviceInfo.setDeviceModel(request.getDeviceModel());
        deviceInfo.setManufacturer(request.getManufacturer());
        deviceInfo.setOsVersion(request.getOsVersion());
        deviceInfo.setSdkVersion(request.getSdkVersion());
        deviceInfo.setSerialNumber(request.getSerialNumber());
        deviceInfo.setUniqueIdentifier(request.getUniqueIdentifier());
        deviceInfo.setImei(request.getImei());
        deviceInfo.setDeviceType(request.getDeviceType());
        deviceInfo.setDevice(enrolledDevice);

        return deviceInfoRepository.save(deviceInfo);
    }

    @Override
    @Transactional
    public List<InstalledApp> saveAppInventory(AppInventoryRequest request) {
        log.info("Saving app inventory for device: {}, apps count: {}",
                request.getDeviceId(), request.getApps().size());

        EnrolledDevice enrolledDevice = enrolledDeviceRepository.findByDeviceId(request.getDeviceId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Device not enrolled: " + request.getDeviceId()));

        // Remove old inventory and replace with new
        installedAppRepository.deleteByDeviceId(request.getDeviceId());

        List<InstalledApp> apps = new ArrayList<>();
        for (AppInventoryRequest.AppDetail appDetail : request.getApps()) {
            InstalledApp app = InstalledApp.builder()
                    .deviceId(request.getDeviceId())
                    .appName(appDetail.getAppName())
                    .packageName(appDetail.getPackageName())
                    .versionName(appDetail.getVersionName())
                    .versionCode(appDetail.getVersionCode())
                    .installationSource(appDetail.getInstallationSource())
                    .isSystemApp(appDetail.getIsSystemApp())
                    .category(appDetail.getCategory())
                    .device(enrolledDevice)
                    .build();
            apps.add(app);
        }

        return installedAppRepository.saveAll(apps);
    }

    @Override
    public List<EnrolledDevice> getAllDevices() {
        return enrolledDeviceRepository.findAll();
    }

    @Override
    public EnrolledDevice getDeviceByDeviceId(String deviceId) {
        return enrolledDeviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));
    }

    @Override
    public List<InstalledApp> getAppsByDeviceId(String deviceId) {
        return installedAppRepository.findByDeviceId(deviceId);
    }

    // ==================== Command Management ====================

    @Override
    @Transactional
    public DeviceCommand sendCommand(DeviceCommandRequest request) {
        log.info("Sending command {} to device {}", request.getCommandType(), request.getDeviceId());

        EnrolledDevice enrolledDevice = enrolledDeviceRepository.findByDeviceId(request.getDeviceId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Device not enrolled: " + request.getDeviceId()));

        DeviceCommand command = DeviceCommand.builder()
                .deviceId(request.getDeviceId())
                .commandType(request.getCommandType().toUpperCase())
                .payload(request.getPayload())
                .status("PENDING")
                .device(enrolledDevice)
                .build();

        return deviceCommandRepository.save(command);
    }

    @Override
    public List<DeviceCommand> getPendingCommands(String deviceId) {
        return deviceCommandRepository.findByDeviceIdAndStatusOrderByCreatedAtAsc(deviceId, "PENDING");
    }

    @Override
    @Transactional
    public DeviceCommand acknowledgeCommand(Long commandId, String status) {
        DeviceCommand command = deviceCommandRepository.findById(commandId)
                .orElseThrow(() -> new IllegalArgumentException("Command not found: " + commandId));
        command.setStatus(status);
        command.setExecutedAt(LocalDateTime.now());
        return deviceCommandRepository.save(command);
    }

    @Override
    public List<DeviceCommand> getCommandHistory(String deviceId) {
        return deviceCommandRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId);
    }

    // ==================== Location Tracking ====================

    @Override
    @Transactional
    public DeviceLocation saveLocation(LocationUpdateRequest request) {
        log.info("Saving location for device {}: ({}, {})",
                request.getDeviceId(), request.getLatitude(), request.getLongitude());

        EnrolledDevice enrolledDevice = enrolledDeviceRepository.findByDeviceId(request.getDeviceId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Device not enrolled: " + request.getDeviceId()));

        DeviceLocation location = DeviceLocation.builder()
                .deviceId(request.getDeviceId())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .accuracy(request.getAccuracy())
                .altitude(request.getAltitude())
                .speed(request.getSpeed())
                .provider(request.getProvider())
                .batteryLevel(request.getBatteryLevel())
                .device(enrolledDevice)
                .build();

        return deviceLocationRepository.save(location);
    }

    @Override
    public DeviceLocation getLatestLocation(String deviceId) {
        return deviceLocationRepository.findTopByDeviceIdOrderByRecordedAtDesc(deviceId)
                .orElse(null);
    }

    @Override
    public List<DeviceLocation> getLocationHistory(String deviceId) {
        return deviceLocationRepository.findByDeviceIdOrderByRecordedAtDesc(deviceId);
    }
}
