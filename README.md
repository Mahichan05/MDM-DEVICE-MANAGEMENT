# Android Enterprise Device Management (MDM) Solution

A prototype Android Enterprise device management solution that supports:
- Device enrollment using QR code or enrollment token
- Device information collection
- Installed application inventory collection
- Secure transmission of collected data to a backend server

## Project Structure

```
├── mdm-backend/    # Spring Boot REST API with PostgreSQL
└── mdm-app/        # Android Device Owner application (Kotlin)
```

## Architecture

```
Android Device Owner App  --REST-->  Spring Boot Backend  -->  PostgreSQL Database
```

## Tech Stack

### Backend
- Java 17 / Spring Boot 3
- PostgreSQL
- JPA / Hibernate
- Basic Authentication

### Android App
- Kotlin
- Minimum SDK 26
- Device Owner Mode (Android Enterprise)
- Retrofit HTTP client
- WorkManager for background sync

## Getting Started

### Backend
1. Install PostgreSQL and create database `mdm_db`
2. Configure `application.properties`
3. Run: `./mvnw spring-boot:run`

### Android App
1. Build APK via Android Studio
2. Factory reset device
3. Provision using QR code or enrollment token
