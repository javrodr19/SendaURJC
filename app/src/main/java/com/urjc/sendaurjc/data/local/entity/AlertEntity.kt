package com.urjc.sendaurjc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.urjc.sendaurjc.domain.model.AlertType
import com.urjc.sendaurjc.domain.model.CriticalAlert

@Entity(tableName = "critical_alerts")
data class AlertEntity(
    @PrimaryKey val id: String,
    val type: String,
    val luminariaId: String?,
    val zoneId: String?,
    val timestamp: Long,
    val processed: Boolean
) {
    fun toDomain() = CriticalAlert(
        id = id,
        type = AlertType.valueOf(type),
        luminariaId = luminariaId,
        zoneId = zoneId,
        timestamp = timestamp,
        processed = processed
    )

    companion object {
        fun fromDomain(a: CriticalAlert) = AlertEntity(
            id = a.id,
            type = a.type.name,
            luminariaId = a.luminariaId,
            zoneId = a.zoneId,
            timestamp = a.timestamp,
            processed = a.processed
        )
    }
}
