package com.mdm.service;

import com.mdm.dto.EnrollRequest;
import com.mdm.dto.DeviceInfoRequest;
import com.mdm.dto.AppInventoryRequest;
import com.mdm.dto.DeviceCommandRequest;
import com.mdm.dto.LocationUpdateRequest;
import com.mdm.entity.EnrolledDevice;
import com.mdm.entity.DeviceInfo;
import com.mdm.entity.DeviceCommand;
import com.mdm.entity.DeviceLocation;
import com.mdm.entity.InstalledApp;

import java.util.List;

public interface DeviceManagementService {

    EnrolledDevice enrollDevice(EnrollRequest request);

    DeviceInfo saveDeviceInfo(DeviceInfoRequest request);

    List<InstalledApp> saveAppInventory(AppInventoryRequest request);

    List<EnrolledDevice> getAllDevices();

    EnrolledDevice getDeviceByDeviceId(String deviceId);

    List<InstalledApp> getAppsByDeviceId(String deviceId);

    // Command management
    DeviceCommand sendCommand(DeviceCommandRequest request);

    List<DeviceCommand> getPendingCommands(String deviceId);

    DeviceCommand acknowledgeCommand(Long commandId, String status);

    List<DeviceCommand> getCommandHistory(String deviceId);

    // Location tracking
    DeviceLocation saveLocation(LocationUpdateRequest request);

    DeviceLocation getLatestLocation(String deviceId);

    List<DeviceLocation> getLocationHistory(String deviceId);
}
