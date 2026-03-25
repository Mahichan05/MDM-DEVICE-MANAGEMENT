package com.mdm.devicemanager.data.collector

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.mdm.devicemanager.admin.MdmDeviceAdminReceiver
import com.mdm.devicemanager.data.model.DeviceInfoRequest
import java.util.UUID

/**
 * Collects device hardware and software information.
 * Handles Android 10+ API restrictions for IMEI and serial number.
 */
class DeviceInfoCollector(private val context: Context) {

    companion object {
        private const val TAG = "DeviceInfoCollector"
        private const val PREF_NAME = "mdm_prefs"
        private const val KEY_DEVICE_UUID = "device_uuid"
    }

    fun collect(deviceId: String): DeviceInfoRequest {
        return DeviceInfoRequest(
            deviceId = deviceId,
            deviceModel = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            osVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            serialNumber = getSerialNumber(),
            uniqueIdentifier = getOrCreateUniqueId(),
            imei = getImei(),
            deviceType = getDeviceType()
        )
    }

    /**
     * Get serial number. On Android 10+, this requires Device Owner permission.
     */
    private fun getSerialNumber(): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+: Only Device Owner can access serial number
                val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                if (dpm.isDeviceOwnerApp(context.packageName)) {
                    Build.getSerial()
                } else {
                    null
                }
            } else {
                @Suppress("DEPRECATION")
                Build.SERIAL
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Cannot access serial number: ${e.message}")
            null
        }
    }

    /**
     * Get IMEI. Restricted on Android 10+ even for Device Owner apps.
     */
    private fun getImei(): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+: IMEI access is restricted
                Log.i(TAG, "IMEI access restricted on Android 10+")
                null
            } else {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    @Suppress("DEPRECATION")
                    tm.deviceId
                } else {
                    null
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Cannot access IMEI: ${e.message}")
            null
        }
    }

    /**
     * Generate or retrieve a persistent unique device identifier (UUID).
     */
    private fun getOrCreateUniqueId(): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var uuid = prefs.getString(KEY_DEVICE_UUID, null)
        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_UUID, uuid).apply()
        }
        return uuid
    }

    /**
     * Classify device type based on screen size and telephony capability.
     */
    private fun getDeviceType(): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        return when (tm?.phoneType) {
            TelephonyManager.PHONE_TYPE_GSM,
            TelephonyManager.PHONE_TYPE_CDMA -> "Phone"
            else -> {
                val config = context.resources.configuration
                val screenWidthDp = config.screenWidthDp
                if (screenWidthDp >= 600) "Tablet" else "Phone"
            }
        }
    }
}
