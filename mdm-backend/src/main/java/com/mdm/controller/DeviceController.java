package com.mdm.controller;

import com.mdm.dto.ApiResponse;
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
import com.mdm.service.DeviceManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DeviceController {

    private final DeviceManagementService deviceManagementService;

    /**
     * POST /enroll - Enroll a device
     */
    @PostMapping("/enroll")
    public ResponseEntity<ApiResponse> enrollDevice(@Valid @RequestBody EnrollRequest request) {
        log.info("Received enrollment request for device: {}", request.getDeviceId());
        EnrolledDevice device = deviceManagementService.enrollDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Device enrolled successfully", device.getDeviceId()));
    }

    /**
     * POST /device-info - Submit device information
     */
    @PostMapping("/device-info")
    public ResponseEntity<ApiResponse> submitDeviceInfo(@Valid @RequestBody DeviceInfoRequest request) {
        log.info("Received device info for: {}", request.getDeviceId());
        DeviceInfo info = deviceManagementService.saveDeviceInfo(request);
        return ResponseEntity.ok(ApiResponse.success("Device info saved successfully", info.getId()));
    }

    /**
     * POST /app-inventory - Submit installed applications inventory
     */
    @PostMapping("/app-inventory")
    public ResponseEntity<ApiResponse> submitAppInventory(@Valid @RequestBody AppInventoryRequest request) {
        log.info("Received app inventory for device: {}, count: {}",
                request.getDeviceId(), request.getApps().size());
        List<InstalledApp> apps = deviceManagementService.saveAppInventory(request);
        return ResponseEntity.ok(ApiResponse.success(
                "App inventory saved successfully", apps.size() + " apps recorded"));
    }

    /**
     * GET /devices - List all enrolled devices (for dashboard)
     */
    @GetMapping("/devices")
    public ResponseEntity<List<EnrolledDevice>> getAllDevices() {
        return ResponseEntity.ok(deviceManagementService.getAllDevices());
    }

    /**
     * GET /devices/{deviceId} - Get device details
     */
    @GetMapping("/devices/{deviceId}")
    public ResponseEntity<EnrolledDevice> getDevice(@PathVariable String deviceId) {
        return ResponseEntity.ok(deviceManagementService.getDeviceByDeviceId(deviceId));
    }

    /**
     * GET /devices/{deviceId}/apps - Get apps for a device
     */
    @GetMapping("/devices/{deviceId}/apps")
    public ResponseEntity<List<InstalledApp>> getDeviceApps(@PathVariable String deviceId) {
        return ResponseEntity.ok(deviceManagementService.getAppsByDeviceId(deviceId));
    }

    // ==================== Device Commands (Find Hub) ====================

    /**
     * POST /command - Send a command to a device (FACTORY_RESET, REBOOT, ALARM, LOCK, STOP_ALARM)
     */
    @PostMapping("/command")
    public ResponseEntity<ApiResponse> sendCommand(@Valid @RequestBody DeviceCommandRequest request) {
        log.info("Sending command {} to device {}", request.getCommandType(), request.getDeviceId());
        DeviceCommand command = deviceManagementService.sendCommand(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Command queued successfully", command.getId()));
    }

    /**
     * GET /commands/{deviceId}/pending - Get pending commands for a device (polled by the app)
     */
    @GetMapping("/commands/{deviceId}/pending")
    public ResponseEntity<List<DeviceCommand>> getPendingCommands(@PathVariable String deviceId) {
        return ResponseEntity.ok(deviceManagementService.getPendingCommands(deviceId));
    }

    /**
     * POST /commands/{commandId}/ack - Acknowledge a command execution
     */
    @PostMapping("/commands/{commandId}/ack")
    public ResponseEntity<ApiResponse> acknowledgeCommand(
            @PathVariable Long commandId,
            @RequestParam(defaultValue = "EXECUTED") String status) {
        DeviceCommand command = deviceManagementService.acknowledgeCommand(commandId, status);
        return ResponseEntity.ok(ApiResponse.success("Command acknowledged", command.getCommandType()));
    }

    /**
     * GET /commands/{deviceId}/history - Get command history for a device
     */
    @GetMapping("/commands/{deviceId}/history")
    public ResponseEntity<List<DeviceCommand>> getCommandHistory(@PathVariable String deviceId) {
        return ResponseEntity.ok(deviceManagementService.getCommandHistory(deviceId));
    }

    // ==================== Location Tracking ====================

    /**
     * POST /location - Submit device location
     */
    @PostMapping("/location")
    public ResponseEntity<ApiResponse> submitLocation(@Valid @RequestBody LocationUpdateRequest request) {
        log.info("Location update for device {}: ({}, {})",
                request.getDeviceId(), request.getLatitude(), request.getLongitude());
        DeviceLocation location = deviceManagementService.saveLocation(request);
        return ResponseEntity.ok(ApiResponse.success("Location saved", location.getId()));
    }

    /**
     * GET /location/{deviceId} - Get latest location for a device
     */
    @GetMapping("/location/{deviceId}")
    public ResponseEntity<DeviceLocation> getLatestLocation(@PathVariable String deviceId) {
        DeviceLocation location = deviceManagementService.getLatestLocation(deviceId);
        if (location == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(location);
    }

    /**
     * GET /location/{deviceId}/history - Get location history
     */
    @GetMapping("/location/{deviceId}/history")
    public ResponseEntity<List<DeviceLocation>> getLocationHistory(@PathVariable String deviceId) {
        return ResponseEntity.ok(deviceManagementService.getLocationHistory(deviceId));
    }
}
