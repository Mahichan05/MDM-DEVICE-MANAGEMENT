package com.mdm.devicemanager.ui

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import android.content.ComponentName
import com.mdm.devicemanager.admin.MdmDeviceAdminReceiver
import com.mdm.devicemanager.data.api.RetrofitClient
import com.mdm.devicemanager.data.collector.AppInventoryCollector
import com.mdm.devicemanager.data.collector.DeviceInfoCollector
import com.mdm.devicemanager.data.model.*
import com.mdm.devicemanager.data.sync.SyncManager
import com.mdm.devicemanager.service.LocationTracker
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _isDeviceOwner = MutableLiveData<Boolean>()
    val isDeviceOwner: LiveData<Boolean> = _isDeviceOwner

    private val _isDeviceAdmin = MutableLiveData<Boolean>()
    val isDeviceAdmin: LiveData<Boolean> = _isDeviceAdmin

    private val _deviceInfo = MutableLiveData<DeviceInfoRequest?>()
    val deviceInfo: LiveData<DeviceInfoRequest?> = _deviceInfo

    private val _appList = MutableLiveData<List<AppDetail>>()
    val appList: LiveData<List<AppDetail>> = _appList

    private val _syncStatus = MutableLiveData<SyncStatus>()
    val syncStatus: LiveData<SyncStatus> = _syncStatus

    private val _deviceId = MutableLiveData<String>()
    val deviceId: LiveData<String> = _deviceId

    private val _locationStatus = MutableLiveData<String>()
    val locationStatus: LiveData<String> = _locationStatus

    private val context: Context get() = getApplication()

    init {
        checkDeviceOwnerStatus()
        collectDeviceData()
        syncNow()
    }

    private fun checkDeviceOwnerStatus() {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        _isDeviceOwner.value = dpm.isDeviceOwnerApp(context.packageName)
        val adminComponent = ComponentName(context, MdmDeviceAdminReceiver::class.java)
        _isDeviceAdmin.value = dpm.isAdminActive(adminComponent)
    }

    fun collectDeviceData() {
        val collector = DeviceInfoCollector(context)
        // Use the unique ID as device identifier
        val prefs = context.getSharedPreferences("mdm_prefs", Context.MODE_PRIVATE)
        var id = prefs.getString("device_uuid", null)
        if (id == null) {
            id = java.util.UUID.randomUUID().toString()
            prefs.edit().putString("device_uuid", id).apply()
        }
        _deviceId.value = id

        val info = collector.collect(id)
        _deviceInfo.value = info

        val appCollector = AppInventoryCollector(context)
        _appList.value = appCollector.collect()
    }

    fun syncNow() {
        val id = _deviceId.value ?: return
        _syncStatus.value = SyncStatus.SYNCING

        viewModelScope.launch {
            try {
                val api = RetrofitClient.apiService

                // Enroll
                val enrollMethod = if (_isDeviceOwner.value == true) "QR_CODE" else "TOKEN"
                val enrollResponse = api.enrollDevice(
                    EnrollRequest(deviceId = id, enrollmentToken = null, enrollmentMethod = enrollMethod)
                )
                if (!enrollResponse.isSuccessful) {
                    _syncStatus.value = SyncStatus.FAILED
                    return@launch
                }

                // Device Info
                val info = _deviceInfo.value
                if (info != null) {
                    val infoResponse = api.submitDeviceInfo(info)
                    if (!infoResponse.isSuccessful) {
                        _syncStatus.value = SyncStatus.FAILED
                        return@launch
                    }
                }

                // App Inventory
                val apps = _appList.value
                if (!apps.isNullOrEmpty()) {
                    val inventoryResponse = api.submitAppInventory(
                        AppInventoryRequest(deviceId = id, apps = apps)
                    )
                    if (!inventoryResponse.isSuccessful) {
                        _syncStatus.value = SyncStatus.FAILED
                        return@launch
                    }
                }

                // Submit location
                val locationTracker = LocationTracker(context)
                val locResult = locationTracker.fetchAndSubmitLocation(id)
                if (locResult) {
                    _locationStatus.value = "Location sent to server"
                } else {
                    _locationStatus.value = "Location unavailable"
                }

                _syncStatus.value = SyncStatus.SUCCESS

                // Schedule periodic sync
                SyncManager.schedulePeriodicSync(context, id)

            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.FAILED
            }
        }
    }

    enum class SyncStatus {
        IDLE, SYNCING, SUCCESS, FAILED
    }
}
