# QR Code Provisioning Guide

## Overview
This guide explains how to provision an Android device in Device Owner mode using QR code-based provisioning.

## Prerequisites
1. A factory-reset Android device (Android 7.0+)
2. The MDM app APK installed on a web server or accessible URL
3. WiFi network available during setup

## QR Code Content

The QR code must contain a JSON payload with the provisioning parameters. 
See `qr-provisioning-config.json` for the template.

### Required Fields
- `android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME`: The component name of the DeviceAdminReceiver
- WiFi configuration for the device to connect during provisioning

### Steps to Generate QR Code

1. Edit `qr-provisioning-config.json` with your WiFi credentials and APK download URL
2. Go to a QR code generator website (e.g., https://www.qr-code-generator.com/)
3. Paste the JSON content
4. Generate and print the QR code

## Provisioning Steps

1. **Factory reset** the Android device
2. On the welcome screen, **tap 6 times** on the screen to trigger QR code reader
3. Connect to WiFi if prompted
4. **Scan the QR code** with the provisioning config
5. The device will:
   - Download and install the MDM app
   - Set it as Device Owner
   - Complete provisioning
6. The `ProvisioningCompleteActivity` will appear
7. Tap **Continue** to go to the main dashboard

## Alternative: ADB Provisioning (for development)

For testing without factory reset, you can set Device Owner via ADB:

```bash
# Install the APK
adb install mdm-app.apk

# Set as Device Owner
adb shell dpm set-device-owner com.mdm.devicemanager/com.mdm.devicemanager.admin.MdmDeviceAdminReceiver
```

**Note:** ADB method requires:
- No accounts configured on the device
- No existing Device Owner
- The app must already be installed

## Token-Based Enrollment

The app also supports manual token-based enrollment:
1. Install the app on the device
2. Open the app
3. The app generates a unique device UUID
4. Press "Sync Now" to enroll with the backend
5. The enrollment method will be recorded as "TOKEN"

## Troubleshooting

| Issue | Solution |
|-------|----------|
| QR reader doesn't appear | Ensure device is running Android 7.0+ and is factory reset |
| "Not allowed to set device owner" | Remove all accounts and existing device admins |
| Provisioning fails | Check WiFi, APK URL, and component name in QR config |
| ADB command fails | Ensure no Google accounts are signed in |
