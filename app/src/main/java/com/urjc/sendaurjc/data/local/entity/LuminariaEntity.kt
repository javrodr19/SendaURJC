package com.urjc.sendaurjc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.urjc.sendaurjc.domain.model.Luminaria
import com.urjc.sendaurjc.domain.model.LuminariaStatus

@Entity(tableName = "luminarias")
data class LuminariaEntity(
    @PrimaryKey val id: String,
    val zoneId: String,
    val status: String,
    val lightIntensity: Int,
    val ambientIllumination: Double,
    val powerWatts: Double,
    val cumulativeConsumption: Double,
    val lastUpdated: Long
) {
    fun toDomain() = Luminaria(
        id = id,
        zoneId = zoneId,
        status = LuminariaStatus.valueOf(status),
        lightIntensity = lightIntensity,
        ambientIllumination = ambientIllumination,
        powerWatts = powerWatts,
        cumulativeConsumption = cumulativeConsumption,
        lastUpdated = lastUpdated
    )

    companion object {
        fun fromDomain(d: Luminaria) = LuminariaEntity(
            id = d.id,
            zoneId = d.zoneId,
            status = d.status.name,
            lightIntensity = d.lightIntensity,
            ambientIllumination = d.ambientIllumination,
            powerWatts = d.powerWatts,
            cumulativeConsumption = d.cumulativeConsumption,
            lastUpdated = d.lastUpdated
        )
    }
}

@Entity(tableName = "luminaria_history")
data class LuminariaHistoryEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val luminariaId: String,
    val status: String,
    val lightIntensity: Int,
    val ambientIllumination: Double,
    val timestamp: Long
)
