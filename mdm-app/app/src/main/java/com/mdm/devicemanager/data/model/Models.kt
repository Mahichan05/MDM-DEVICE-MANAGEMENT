package com.mdm.devicemanager.data.model

import com.google.gson.annotations.SerializedName

data class EnrollRequest(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("enrollmentToken") val enrollmentToken: String?,
    @SerializedName("enrollmentMethod") val enrollmentMethod: String
)

data class DeviceInfoRequest(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("deviceModel") val deviceModel: String,
    @SerializedName("manufacturer") val manufacturer: String,
    @SerializedName("osVersion") val osVersion: String,
    @SerializedName("sdkVersion") val sdkVersion: Int,
    @SerializedName("serialNumber") val serialNumber: String?,
    @SerializedName("uniqueIdentifier") val uniqueIdentifier: String,
    @SerializedName("imei") val imei: String?,
    @SerializedName("deviceType") val deviceType: String?
)

data class AppInventoryRequest(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("apps") val apps: List<AppDetail>
)

data class AppDetail(
    @SerializedName("appName") val appName: String,
    @SerializedName("packageName") val packageName: String,
    @SerializedName("versionName") val versionName: String,
    @SerializedName("versionCode") val versionCode: Long,
    @SerializedName("installationSource") val installationSource: String?,
    @SerializedName("isSystemApp") val isSystemApp: Boolean,
    @SerializedName("category") val category: String = "Other"
)

data class ApiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Any?
)

// ==================== Find Hub Models ====================

data class DeviceCommand(
    @SerializedName("id") val id: Long,
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("commandType") val commandType: String,
    @SerializedName("payload") val payload: String? = null,
    @SerializedName("status") val status: String,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("executedAt") val executedAt: String?
)

data class LocationUpdateRequest(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("accuracy") val accuracy: Float?,
    @SerializedName("altitude") val altitude: Double?,
    @SerializedName("speed") val speed: Float?,
    @SerializedName("provider") val provider: String?,
    @SerializedName("batteryLevel") val batteryLevel: Int?
)

data class CommandAckRequest(
    @SerializedName("status") val status: String
)
