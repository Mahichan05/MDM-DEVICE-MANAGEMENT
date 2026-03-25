package com.mdm.devicemanager.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mdm.devicemanager.data.api.RetrofitClient
import com.mdm.devicemanager.data.collector.AppInventoryCollector
import com.mdm.devicemanager.data.collector.DeviceInfoCollector
import com.mdm.devicemanager.data.model.AppInventoryRequest
import com.mdm.devicemanager.data.model.EnrollRequest

/**
 * WorkManager Worker that performs background synchronization of device data.
 * Sends enrollment, device info, and app inventory to the backend server.
 */
class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "SyncWorker"
        const val WORK_NAME = "mdm_sync_work"
        const val KEY_DEVICE_ID = "device_id"
        const val KEY_ENROLLMENT_METHOD = "enrollment_method"
    }

    override suspend fun doWork(): Result {
        val deviceId = inputData.getString(KEY_DEVICE_ID)
        val enrollmentMethod = inputData.getString(KEY_ENROLLMENT_METHOD) ?: "TOKEN"

        if (deviceId.isNullOrEmpty()) {
            Log.e(TAG, "Device ID is missing")
            return Result.failure()
        }

        Log.i(TAG, "Starting sync for device: $deviceId")

        return try {
            val api = RetrofitClient.apiService

            // Step 1: Enroll device
            Log.i(TAG, "Step 1: Enrolling device...")
            val enrollResponse = api.enrollDevice(
                EnrollRequest(
                    deviceId = deviceId,
                    enrollmentToken = null,
                    enrollmentMethod = enrollmentMethod
                )
            )
            if (!enrollResponse.isSuccessful) {
                Log.e(TAG, "Enrollment failed: ${enrollResponse.code()}")
                return Result.retry()
            }
            Log.i(TAG, "Enrollment successful")

            // Step 2: Send device info
            Log.i(TAG, "Step 2: Sending device info...")
            val deviceInfoCollector = DeviceInfoCollector(applicationContext)
            val deviceInfo = deviceInfoCollector.collect(deviceId)
            val infoResponse = api.submitDeviceInfo(deviceInfo)
            if (!infoResponse.isSuccessful) {
                Log.e(TAG, "Device info submission failed: ${infoResponse.code()}")
                return Result.retry()
            }
            Log.i(TAG, "Device info sent successfully")

            // Step 3: Send app inventory
            Log.i(TAG, "Step 3: Sending app inventory...")
            val appCollector = AppInventoryCollector(applicationContext)
            val apps = appCollector.collect()
            val inventoryResponse = api.submitAppInventory(
                AppInventoryRequest(deviceId = deviceId, apps = apps)
            )
            if (!inventoryResponse.isSuccessful) {
                Log.e(TAG, "App inventory submission failed: ${inventoryResponse.code()}")
                return Result.retry()
            }
            Log.i(TAG, "App inventory sent successfully (${apps.size} apps)")

            Log.i(TAG, "Sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed with exception", e)
            Result.retry()
        }
    }
}
