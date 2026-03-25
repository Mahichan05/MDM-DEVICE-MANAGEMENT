# MDM Android App

Android Enterprise Device Owner application for device management.

## Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 34
- Physical Android device (for Device Owner testing)

## Building

1. Open the `mdm-app` folder in Android Studio
2. Sync Gradle
3. Build APK: Build > Build Bundle(s) / APK(s) > Build APK(s)

## Configuration

Edit `app/build.gradle.kts`:
- `BASE_URL` - Backend server URL (default: `http://10.0.2.2:8080/` for emulator)
- `AUTH_USERNAME` - Backend auth username
- `AUTH_PASSWORD` - Backend auth password

For physical device, change `BASE_URL` to your server's IP address:
```kotlin
buildConfigField("String", "BASE_URL", "\"http://192.168.1.100:8080/\"")
```

## Features

1. **Device Owner Mode** - Full device management capabilities
2. **Device Info Collection** - Model, manufacturer, OS version, serial, IMEI
3. **App Inventory** - Lists all installed apps with metadata
4. **Background Sync** - WorkManager-based periodic sync every 6 hours
5. **REST API Integration** - Retrofit client with Basic Auth

## Provisioning

See [PROVISIONING_GUIDE.md](PROVISIONING_GUIDE.md) for detailed provisioning instructions.

### Quick Start (ADB)
```bash
adb install app-debug.apk
adb shell dpm set-device-owner com.mdm.devicemanager/com.mdm.devicemanager.admin.MdmDeviceAdminReceiver
```

## Project Structure
```
app/src/main/java/com/mdm/devicemanager/
├── MdmApplication.kt           # Application class
├── admin/
│   └── MdmDeviceAdminReceiver.kt  # Device Owner receiver
├── data/
│   ├── api/
│   │   ├── MdmApiService.kt    # Retrofit API interface
│   │   └── RetrofitClient.kt   # HTTP client with auth
│   ├── collector/
│   │   ├── DeviceInfoCollector.kt    # Hardware info collector
│   │   └── AppInventoryCollector.kt  # App list collector
│   ├── model/
│   │   └── Models.kt           # Data classes
│   └── sync/
│       ├── SyncWorker.kt       # Background sync worker
│       └── SyncManager.kt      # Sync scheduling
└── ui/
    ├── MainActivity.kt         # Main dashboard
    ├── MainViewModel.kt        # ViewModel
    ├── AppListAdapter.kt       # RecyclerView adapter
    └── ProvisioningCompleteActivity.kt  # Post-provisioning screen
```
