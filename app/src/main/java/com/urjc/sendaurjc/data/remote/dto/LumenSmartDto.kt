package com.urjc.sendaurjc.data.remote.dto

import com.google.gson.annotations.SerializedName

// RF14: all exchange in JSON UTF-8

data class LuminariaStatusDto(
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String,         // RF1
    @SerializedName("intensity") val intensity: Int,      // RF2: 0-100
    @SerializedName("lux") val lux: Double,               // RF3
    @SerializedName("power_w") val powerWatts: Double,    // RF13
    @SerializedName("cumulative_kwh") val cumulativeKwh: Double, // RF13
    @SerializedName("zone_id") val zoneId: String,
    @SerializedName("timestamp") val timestamp: Long
)

data class PresenceEventDto(
    @SerializedName("sensor_id") val sensorId: String,
    @SerializedName("event") val event: String,           // RF4: PRESENCIA/NO_PRESENCIA
    @SerializedName("timestamp") val timestamp: Long
)

data class SensorActivationDto(
    @SerializedName("sensor_id") val sensorId: String,
    @SerializedName("count") val count: Int,              // RF5
    @SerializedName("interval_start") val intervalStart: Long,
    @SerializedName("interval_end") val intervalEnd: Long
)

data class ZoneTrafficDto(
    @SerializedName("zone_id") val zoneId: String,
    @SerializedName("traffic_index") val trafficIndex: Int  // RF6: 0-100
)

data class CriticalAlertDto(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,             // RF10
    @SerializedName("luminaria_id") val luminariaId: String?,
    @SerializedName("zone_id") val zoneId: String?,
    @SerializedName("timestamp") val timestamp: Long
)

data class WebhookPayloadDto(
    @SerializedName("event_type") val eventType: String,  // RF15
    @SerializedName("payload") val payload: Map<String, Any>,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("signature") val signature: String    // RF17: HMAC-SHA256
)

data class OAuthTokenDto(
    @SerializedName("access_token") val accessToken: String, // RF16
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Long
)

data class OAuthRequestDto(
    @SerializedName("grant_type") val grantType: String = "client_credentials",
    @SerializedName("client_id") val clientId: String,
    @SerializedName("client_secret") val clientSecret: String
)
