# MDM Backend

Spring Boot REST API for Android Enterprise Device Management.

## Prerequisites
- Java 17+
- PostgreSQL 14+
- Maven 3.9+

## Database Setup

```sql
CREATE DATABASE mdm_db;
```

## Configuration

Edit `src/main/resources/application.properties`:
- `spring.datasource.url` - PostgreSQL connection URL
- `spring.datasource.username` - DB username
- `spring.datasource.password` - DB password
- `mdm.auth.username` - Basic auth username for API
- `mdm.auth.password` - Basic auth password for API

## Running

```bash
./mvnw spring-boot:run
```

Server starts on `http://localhost:8080`

## API Endpoints

### POST /enroll
Enroll a device.
```json
{
  "deviceId": "uuid-string",
  "enrollmentToken": "optional-token",
  "enrollmentMethod": "QR_CODE"
}
```

### POST /device-info
Submit device hardware info.
```json
{
  "deviceId": "uuid-string",
  "deviceModel": "Pixel 7",
  "manufacturer": "Google",
  "osVersion": "14",
  "sdkVersion": 34,
  "serialNumber": "ABC123",
  "uniqueIdentifier": "uuid-string",
  "imei": null,
  "deviceType": "Phone"
}
```

### POST /app-inventory
Submit installed applications.
```json
{
  "deviceId": "uuid-string",
  "apps": [
    {
      "appName": "Chrome",
      "packageName": "com.android.chrome",
      "versionName": "120.0",
      "versionCode": 120,
      "installationSource": "com.android.vending",
      "isSystemApp": false
    }
  ]
}
```

### GET /devices
List all enrolled devices.

### GET /devices/{deviceId}
Get specific device details.

### GET /devices/{deviceId}/apps
Get installed apps for a device.

## Web Dashboard

Access `http://localhost:8080/dashboard` for the web dashboard.

**Credentials:** Same as configured in `application.properties`

## Authentication

All endpoints use HTTP Basic Authentication.
- Username: `mdm-admin` (default)
- Password: `mdm-secret-2024` (default)
