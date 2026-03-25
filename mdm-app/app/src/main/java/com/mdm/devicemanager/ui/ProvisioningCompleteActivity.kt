package com.mdm.devicemanager.ui

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mdm.devicemanager.R
import com.mdm.devicemanager.admin.MdmDeviceAdminReceiver

/**
 * Activity shown after Device Owner provisioning is complete.
 * Launches when ACTION_PROVISIONING_SUCCESSFUL is received.
 */
class ProvisioningCompleteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provisioning_complete)

        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val isDeviceOwner = dpm.isDeviceOwnerApp(packageName)

        val statusText = findViewById<TextView>(R.id.statusText)
        val continueButton = findViewById<Button>(R.id.continueButton)

        if (isDeviceOwner) {
            statusText.text = getString(R.string.provisioning_complete)
            // Enable the device admin for this app
            val componentName = MdmDeviceAdminReceiver.getComponentName(this)
            dpm.setProfileName(componentName, getString(R.string.app_name))
        }

        continueButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
