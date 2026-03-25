package com.mdm.devicemanager.data.api

import com.mdm.devicemanager.data.model.ApiResponse
import com.mdm.devicemanager.data.model.AppInventoryRequest
import com.mdm.devicemanager.data.model.DeviceCommand
import com.mdm.devicemanager.data.model.DeviceInfoRequest
import com.mdm.devicemanager.data.model.EnrollRequest
import com.mdm.devicemanager.data.model.LocationUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MdmApiService {

    @POST("enroll")
    suspend fun enrollDevice(@Body request: EnrollRequest): Response<ApiResponse>

    @POST("device-info")
    suspend fun submitDeviceInfo(@Body request: DeviceInfoRequest): Response<ApiResponse>

    @POST("app-inventory")
    suspend fun submitAppInventory(@Body request: AppInventoryRequest): Response<ApiResponse>

    // Find Hub endpoints
    @GET("commands/{deviceId}/pending")
    suspend fun getPendingCommands(@Path("deviceId") deviceId: String): Response<List<DeviceCommand>>

    @POST("commands/{commandId}/ack")
    suspend fun acknowledgeCommand(
        @Path("commandId") commandId: Long,
        @Query("status") status: String
    ): Response<ApiResponse>

    @POST("location")
    suspend fun submitLocation(@Body request: LocationUpdateRequest): Response<ApiResponse>
}
