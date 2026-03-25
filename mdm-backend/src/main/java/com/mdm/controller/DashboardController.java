package com.mdm.controller;

import com.mdm.entity.EnrolledDevice;
import com.mdm.entity.InstalledApp;
import com.mdm.service.DeviceManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DeviceManagementService deviceManagementService;

    @GetMapping
    public String dashboard(Model model) {
        List<EnrolledDevice> devices = deviceManagementService.getAllDevices();
        model.addAttribute("devices", devices);
        model.addAttribute("totalDevices", devices.size());
        model.addAttribute("activeDevices", devices.stream()
                .filter(d -> "ACTIVE".equals(d.getStatus())).count());
        return "dashboard";
    }

    @GetMapping("/device/{deviceId}")
    public String deviceDetail(@PathVariable String deviceId, Model model) {
        EnrolledDevice device = deviceManagementService.getDeviceByDeviceId(deviceId);
        List<InstalledApp> apps = deviceManagementService.getAppsByDeviceId(deviceId);
        model.addAttribute("device", device);
        model.addAttribute("apps", apps);
        return "device-detail";
    }
}
