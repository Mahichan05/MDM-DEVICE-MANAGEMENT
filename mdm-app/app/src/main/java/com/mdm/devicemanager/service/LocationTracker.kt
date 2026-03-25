package com.mdm.devicemanager.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.mdm.devicemanager.data.api.RetrofitClient
import com.mdm.devicemanager.data.model.LocationUpdateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationTracker(private val context: Context) {

    companion object {
        private const val TAG = "LocationTracker"
        private const val MIN_TIME_MS = 30_000L   // 30 seconds
        private const val MIN_DISTANCE_M = 10f     // 10 meters
    }

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val apiService = RetrofitClient.apiService
    private var isTracking = false

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.d(TAG, "Location update: ${location.latitude}, ${location.longitude}")
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    fun startTracking() {
        if (isTracking) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Location permission not granted")
            return
        }

        try {
            // Try GPS first, fall back to network
            val providers = locationManager.getProviders(true)
            val provider = when {
                providers.contains(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                providers.contains(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> {
                    Log.e(TAG, "No location providers available")
                    return
                }
            }

            locationManager.requestLocationUpdates(provider, MIN_TIME_MS, MIN_DISTANCE_M, locationListener)
            isTracking = true
            Log.i(TAG, "Location tracking started with provider: $provider")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start location tracking", e)
        }
    }

    fun stopTracking() {
        if (!isTracking) return
        locationManager.removeUpdates(locationListener)
        isTracking = false
        Log.i(TAG, "Location tracking stopped")
    }

    suspend fun fetchAndSubmitLocation(deviceId: String): Boolean = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Location permission not granted")
            return@withContext false
        }

        val location = getLastKnownLocation()
        if (location == null) {
            Log.w(TAG, "No last known location available")
            return@withContext false
        }

        val batteryLevel = getBatteryLevel()

        val request = LocationUpdateRequest(
            deviceId = deviceId,
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            altitude = location.altitude,
            speed = location.speed,
            provider = location.provider,
            batteryLevel = batteryLevel
        )

        try {
            val response = apiService.submitLocation(request)
            if (response.isSuccessful) {
                Log.i(TAG, "Location submitted: ${location.latitude}, ${location.longitude}")
                true
            } else {
                Log.e(TAG, "Location submit failed: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit location", e)
            false
        }
    }

    private fun getLastKnownLocation(): Location? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return null

        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null

        for (provider in providers) {
            val loc = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                bestLocation = loc
            }
        }
        return bestLocation
    }

    private fun getBatteryLevel(): Int {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) (level * 100 / scale) else -1
    }
}
