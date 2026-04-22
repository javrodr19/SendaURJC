package com.urjc.sendaurjc.domain.model

enum class LuminariaStatus { ENCENDIDA, APAGADA, AVERIADA, EN_MANTENIMIENTO }

data class Luminaria(
    val id: String,
    val zoneId: String,
    val status: LuminariaStatus,
    val lightIntensity: Int,        // RF2: 0-100 %
    val ambientIllumination: Double, // RF3: lux
    val powerWatts: Double,          // RF13: instantaneous power
    val cumulativeConsumption: Double, // RF13: accumulated consumption
    val lastUpdated: Long = System.currentTimeMillis()
)

data class PresenceEvent(
    val sensorId: String,
    val detected: Boolean,           // RF4: PRESENCIA / NO_PRESENCIA
    val timestamp: Long = System.currentTimeMillis()
)

data class SensorActivation(
    val sensorId: String,
    val count: Int,                  // RF5: aggregated activations
    val intervalStart: Long,
    val intervalEnd: Long
)
