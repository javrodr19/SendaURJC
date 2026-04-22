package com.urjc.sendaurjc.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SsoLoginRequestDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("mfa_code") val mfaCode: String? = null  // 2FA Microsoft Authenticator
)

data class SsoLoginResponseDto(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("surname") val surname: String,
    @SerializedName("role") val role: String,
    @SerializedName("expires_in") val expiresIn: Long
)

data class UserProfileDto(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("surname") val surname: String,
    @SerializedName("role") val role: String,
    @SerializedName("is_volunteer") val isVolunteer: Boolean,
    @SerializedName("profile_complete") val profileComplete: Boolean,
    @SerializedName("total_routes") val totalRoutes: Int,
    @SerializedName("total_companions") val totalCompanions: Int
)

data class TrustedContactDto(
    @SerializedName("id") val id: String,
    @SerializedName("contact_name") val contactName: String,
    @SerializedName("contact_phone") val contactPhone: String,
    @SerializedName("contact_email") val contactEmail: String
)

data class LocationShareDto(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("recipient_id") val recipientId: String,
    @SerializedName("timestamp") val timestamp: Long
)
