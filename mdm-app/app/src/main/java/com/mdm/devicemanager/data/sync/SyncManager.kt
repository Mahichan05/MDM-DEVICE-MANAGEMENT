package com.mdm.devicemanager.data.sync

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Manages WorkManager scheduling for periodic and one-time sync operations.
 */
object SyncManager {

    private const val PERIODIC_SYNC_WORK = "mdm_periodic_sync"

    /**
     * Schedule an immediate one-time sync.
     */
    fun syncNow(context: Context, deviceId: String, enrollmentMethod: String = "TOKEN") {
        val inputData = Data.Builder()
            .putString(SyncWorker.KEY_DEVICE_ID, deviceId)
            .putString(SyncWorker.KEY_ENROLLMENT_METHOD, enrollmentMethod)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                SyncWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
    }

    /**
     * Schedule periodic background sync every 6 hours.
     */
    fun schedulePeriodicSync(context: Context, deviceId: String) {
        val inputData = Data.Builder()
            .putString(SyncWorker.KEY_DEVICE_ID, deviceId)
            .putString(SyncWorker.KEY_ENROLLMENT_METHOD, "TOKEN")
            .build()

        val periodicSync = PeriodicWorkRequestBuilder<SyncWorker>(
            6, TimeUnit.HOURS,
            30, TimeUnit.MINUTES
        )
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                PERIODIC_SYNC_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicSync
            )
    }
}
