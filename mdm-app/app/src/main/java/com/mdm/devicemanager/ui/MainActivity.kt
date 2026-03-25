package com.mdm.devicemanager.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.mdm.devicemanager.R
import com.mdm.devicemanager.data.model.DeviceInfoRequest
import com.mdm.devicemanager.databinding.ActivityMainBinding
import com.mdm.devicemanager.service.CommandPollingWorker
import com.mdm.devicemanager.service.LocationTracker

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var appAdapter: AppListAdapter
    private lateinit var locationTracker: LocationTracker

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        appAdapter = AppListAdapter()
        locationTracker = LocationTracker(this)

        setupUI()
        observeViewModel()
        requestPermissions()
        requestLocationPermission()

        // Schedule command polling worker
        CommandPollingWorker.schedule(this)
    }

    private fun setupUI() {
        // Setup RecyclerView
        binding.appRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = appAdapter
        }

        // Setup tabs
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.deviceInfoContainer.visibility = View.VISIBLE
                        binding.appRecyclerView.visibility = View.GONE
                        binding.findHubContainer.visibility = View.GONE
                    }
                    1 -> {
                        binding.deviceInfoContainer.visibility = View.GONE
                        binding.appRecyclerView.visibility = View.VISIBLE
                        binding.findHubContainer.visibility = View.GONE
                    }
                    2 -> {
                        binding.deviceInfoContainer.visibility = View.GONE
                        binding.appRecyclerView.visibility = View.GONE
                        binding.findHubContainer.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Sync button
        binding.syncButton.setOnClickListener {
            viewModel.syncNow()
        }
    }

    private fun observeViewModel() {
        viewModel.isDeviceOwner.observe(this) { _ ->
            updateAdminStatus()
        }
        viewModel.isDeviceAdmin.observe(this) { _ ->
            updateAdminStatus()
        }

        viewModel.deviceId.observe(this) { id ->
            binding.deviceIdText.text = "Device ID: $id"
        }

        viewModel.deviceInfo.observe(this) { info ->
            if (info != null) {
                populateDeviceInfo(info)
            }
        }

        viewModel.appList.observe(this) { apps ->
            appAdapter.submitList(apps)
        }

        viewModel.syncStatus.observe(this) { status ->
            when (status) {
                MainViewModel.SyncStatus.SYNCING -> {
                    binding.syncButton.isEnabled = false
                    binding.syncStatusText.text = getString(R.string.syncing)
                    binding.syncStatusText.setTextColor(
                        ContextCompat.getColor(this, R.color.text_secondary)
                    )
                }
                MainViewModel.SyncStatus.SUCCESS -> {
                    binding.syncButton.isEnabled = true
                    binding.syncStatusText.text = getString(R.string.sync_success)
                    binding.syncStatusText.setTextColor(
                        ContextCompat.getColor(this, R.color.status_active)
                    )
                    Toast.makeText(this, R.string.sync_success, Toast.LENGTH_SHORT).show()
                }
                MainViewModel.SyncStatus.FAILED -> {
                    binding.syncButton.isEnabled = true
                    binding.syncStatusText.text = getString(R.string.sync_failed)
                    binding.syncStatusText.setTextColor(
                        ContextCompat.getColor(this, R.color.status_inactive)
                    )
                    Toast.makeText(this, R.string.sync_failed, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.syncButton.isEnabled = true
                    binding.syncStatusText.text = ""
                }
            }
        }

        viewModel.locationStatus.observe(this) { status ->
            binding.findHubStatusText.text = status
        }
    }

    private fun updateAdminStatus() {
        val isOwner = viewModel.isDeviceOwner.value ?: false
        val isAdmin = viewModel.isDeviceAdmin.value ?: false
        when {
            isOwner -> {
                binding.deviceOwnerStatus.text = getString(R.string.device_owner_active)
                binding.deviceOwnerStatus.setTextColor(
                    ContextCompat.getColor(this, R.color.status_active)
                )
            }
            isAdmin -> {
                binding.deviceOwnerStatus.text = getString(R.string.device_admin_active)
                binding.deviceOwnerStatus.setTextColor(
                    ContextCompat.getColor(this, R.color.status_active)
                )
            }
            else -> {
                binding.deviceOwnerStatus.text = getString(R.string.device_owner_inactive)
                binding.deviceOwnerStatus.setTextColor(
                    ContextCompat.getColor(this, R.color.status_inactive)
                )
            }
        }
    }

    private fun populateDeviceInfo(info: DeviceInfoRequest) {
        binding.deviceInfoLayout.removeAllViews()

        val infoItems = listOf(
            "Model" to info.deviceModel,
            "Manufacturer" to info.manufacturer,
            "OS Version" to info.osVersion,
            "SDK Version" to info.sdkVersion.toString(),
            "Serial Number" to (info.serialNumber ?: "N/A"),
            "Unique ID" to info.uniqueIdentifier,
            "IMEI" to (info.imei ?: "Restricted (Android 10+)"),
            "Device Type" to (info.deviceType ?: "Unknown")
        )

        for ((label, value) in infoItems) {
            val itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_device_info, binding.deviceInfoLayout, false)
            itemView.findViewById<TextView>(R.id.labelText).text = label
            itemView.findViewById<TextView>(R.id.valueText).text = value
            binding.deviceInfoLayout.addView(itemView)
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_PHONE_STATE)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun requestLocationPermission() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                needed.toTypedArray(),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            locationTracker.startTracking()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                viewModel.collectDeviceData()
            }
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationTracker.startTracking()
                    Toast.makeText(this, "Location tracking enabled", Toast.LENGTH_SHORT).show()

                    // Request background location for Android 10+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                                LOCATION_PERMISSION_REQUEST_CODE + 1
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationTracker.stopTracking()
    }
}
