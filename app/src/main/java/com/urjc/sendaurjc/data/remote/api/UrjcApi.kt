package com.urjc.sendaurjc.data.remote.api

import com.urjc.sendaurjc.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

// RF27: centralized SSO authentication
interface UrjcApi {

    @POST("auth/login")
    suspend fun login(@Body request: SsoLoginRequestDto): Response<SsoLoginResponseDto>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Header("Authorization") token: String
    ): Response<SsoLoginResponseDto>

    @GET("users/me")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<UserProfileDto>              // RF28

    @PUT("users/me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body profile: UserProfileDto
    ): Response<Unit>

    @PUT("users/me/trusted-contact")
    suspend fun setTrustedContact(
        @Header("Authorization") token: String,
        @Body contact: TrustedContactDto
    ): Response<Unit>                        // RF40

    @POST("location/share")
    suspend fun shareLocation(
        @Header("Authorization") token: String,
        @Body payload: LocationShareDto
    ): Response<Unit>                        // RF39

    @POST("incidents")
    suspend fun reportIncident(
        @Header("Authorization") token: String,
        @Body incident: Map<String, Any>
    ): Response<Map<String, String>>         // RF42

    @GET("incidents")
    suspend fun getAllIncidents(
        @Header("Authorization") token: String
    ): Response<List<Map<String, Any>>>      // RF43

    @PUT("incidents/{id}/status")
    suspend fun updateIncidentStatus(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Response<Unit>

    @POST("companions/request")
    suspend fun requestCompanion(
        @Header("Authorization") token: String,
        @Body body: Map<String, Any>
    ): Response<Map<String, String>>

    @PUT("companions/{id}/accept")
    suspend fun acceptCompanion(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>

    @PUT("companions/{id}/reject")
    suspend fun rejectCompanion(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}
