package com.urjc.sendaurjc.domain.model

enum class SafetyEvent { STOP_DETECTED, DEVIATION_DETECTED, USER_CONFIRMED, TIMEOUT, ALERT_SENT }
enum class SafetyState { MONITORING, PRE_ALERT, AWAITING_RESPONSE, ALERT_SENT, SAFE }

data class PreAlert(
    val id: String,
    val userId: String,
    val location: GeoPoint,
    val event: SafetyEvent,
    val timestamp: Long,
    val state: SafetyState = SafetyState.PRE_ALERT
)

data class SafetyConfig(
    val alertWaitSeconds: Int = 30,
    val stopThresholdSeconds: Int = 120,
    val deviationThresholdMeters: Double = 50.0
)
