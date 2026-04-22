package com.urjc.sendaurjc.data.remote.api

import com.urjc.sendaurjc.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

// RF11: periodic REST API queries to LumenSmart
interface LumenSmartApi {

    @POST("oauth2/token")
    suspend fun getOAuthToken(@Body request: OAuthRequestDto): Response<OAuthTokenDto>  // RF16

    @GET("luminarias")
    suspend fun getAllLuminarias(
        @Header("Authorization") token: String
    ): Response<List<LuminariaStatusDto>>

    @GET("luminarias/{id}")
    suspend fun getLuminaria(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<LuminariaStatusDto>

    @GET("zones")
    suspend fun getAllZones(
        @Header("Authorization") token: String
    ): Response<List<ZoneTrafficDto>>

    @GET("alerts")
    suspend fun getCriticalAlerts(
        @Header("Authorization") token: String,
        @Query("since") sinceTimestamp: Long
    ): Response<List<CriticalAlertDto>>     // RF10

    @GET("sensors/activations")
    suspend fun getSensorActivations(
        @Header("Authorization") token: String,
        @Query("interval_start") from: Long,
        @Query("interval_end") to: Long
    ): Response<List<SensorActivationDto>>  // RF5

    @GET("presence")
    suspend fun getPresenceEvents(
        @Header("Authorization") token: String,
        @Query("since") sinceTimestamp: Long
    ): Response<List<PresenceEventDto>>     // RF4
}
