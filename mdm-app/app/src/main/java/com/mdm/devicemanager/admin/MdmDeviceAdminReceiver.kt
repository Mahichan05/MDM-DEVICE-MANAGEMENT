package com.mdm.devicemanager.admin

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

/**
 * DeviceAdminReceiver for Device Owner mode.
 * This receiver handles device admin lifecycle events during provisioning.
 */
class MdmDeviceAdminReceiver : DeviceAdminReceiver() {

    companion object {
        private const val TAG = "MdmDeviceAdminReceiver"

        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, MdmDeviceAdminReceiver::class.java)
        }
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i(TAG, "Device admin enabled")
        Toast.makeText(context, "MDM Device Admin Enabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.i(TAG, "Device admin disabled")
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        Log.i(TAG, "Profile provisioning complete")
    }
}
