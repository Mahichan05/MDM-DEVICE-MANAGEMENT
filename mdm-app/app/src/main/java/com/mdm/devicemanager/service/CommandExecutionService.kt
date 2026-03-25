package com.mdm.devicemanager.service

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.util.Log
import com.mdm.devicemanager.admin.MdmDeviceAdminReceiver
import com.mdm.devicemanager.data.api.RetrofitClient
import com.mdm.devicemanager.data.model.DeviceCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommandExecutionService(private val context: Context) {

    companion object {
        private const val TAG = "CommandExecService"
    }

    private val apiService = RetrofitClient.apiService
    private val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent = ComponentName(context, MdmDeviceAdminReceiver::class.java)

    suspend fun pollAndExecuteCommands(deviceId: String) = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPendingCommands(deviceId)
            if (response.isSuccessful) {
                val commands = response.body() ?: emptyList()
                for (command in commands) {
                    executeCommand(command)
                }
            } else {
                Log.e(TAG, "Failed to fetch commands: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error polling commands", e)
        }
    }

    private suspend fun executeCommand(command: DeviceCommand) {
        Log.i(TAG, "Executing command: ${command.commandType} (id=${command.id})")
        var status = "EXECUTED"
        try {
            when (command.commandType) {
                "FACTORY_RESET" -> performFactoryReset()
                "REBOOT" -> performReboot()
                "ALARM" -> AlarmPlayer.getInstance(context).startAlarm()
                "STOP_ALARM" -> AlarmPlayer.getInstance(context).stopAlarm()
                "LOCK" -> performLockDevice()
                "UNINSTALL_APP" -> performUninstallApp(command.payload)
                else -> {
                    Log.w(TAG, "Unknown command type: ${command.commandType}")
                    status = "FAILED"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Command execution failed: ${command.commandType}", e)
            status = "FAILED"
        }

        // Acknowledge command
        try {
            apiService.acknowledgeCommand(command.id, status)
            Log.i(TAG, "Command ${command.id} acknowledged as $status")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acknowledge command ${command.id}", e)
        }
    }

    private fun performFactoryReset() {
        if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
            Log.w(TAG, "Performing FACTORY RESET")
            devicePolicyManager.wipeData(0)
        } else {
            Log.e(TAG, "Cannot factory reset - not device owner")
            throw SecurityException("App is not device owner")
        }
    }

    private fun performReboot() {
        if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
            Log.w(TAG, "Performing REBOOT")
            devicePolicyManager.reboot(adminComponent)
        } else {
            // Fallback: try PowerManager
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.reboot("MDM Remote Reboot")
        }
    }

    private fun performLockDevice() {
        if (devicePolicyManager.isAdminActive(adminComponent)) {
            devicePolicyManager.lockNow()
            Log.i(TAG, "Device locked")
        } else {
            Log.e(TAG, "Cannot lock - not device admin")
            throw SecurityException("App is not device admin")
        }
    }

    private fun performUninstallApp(packageName: String?) {
        if (packageName.isNullOrBlank()) {
            throw IllegalArgumentException("Package name is required for UNINSTALL_APP")
        }
        Log.i(TAG, "Uninstalling app: $packageName")
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
