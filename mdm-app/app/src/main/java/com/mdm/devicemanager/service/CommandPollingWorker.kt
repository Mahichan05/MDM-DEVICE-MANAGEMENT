package com.mdm.devicemanager.service

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class CommandPollingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "CommandPollingWorker"
        private const val WORK_NAME = "command_polling"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<CommandPollingWorker>(
                15, TimeUnit.MINUTES  // Minimum interval for periodic work
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Log.i(TAG, "Command polling worker scheduled")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Polling for commands...")

        val deviceId = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        // Execute pending commands
        val commandService = CommandExecutionService(applicationContext)
        commandService.pollAndExecuteCommands(deviceId)

        // Submit location
        val locationTracker = LocationTracker(applicationContext)
        locationTracker.fetchAndSubmitLocation(deviceId)

        return Result.success()
    }
}
