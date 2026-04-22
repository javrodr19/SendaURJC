package com.urjc.sendaurjc.domain.model

data class Zone(
    val id: String,
    val name: String,
    val trafficIndex: Int,           // RF6: 0-100 normalised
    val luminariaIds: List<String>,
    val hasPartialCoverage: Boolean = false, // RF22
    val dataQuality: DataQuality = DataQuality.HIGH  // RF23
)

enum class DataQuality { HIGH, MEDIUM, LOW, PARTIAL }

enum class AlertType {
    APAGON, AVERIA, CAMBIO_CRITICO_ESTADO, PRESENCIA_MASIVA
}

data class CriticalAlert(
    val id: String,
    val type: AlertType,
    val luminariaId: String?,
    val zoneId: String?,
    val timestamp: Long,
    val processed: Boolean = false
)
