package com.urjc.sendaurjc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.urjc.sendaurjc.domain.model.DataQuality
import com.urjc.sendaurjc.domain.model.Zone

@Entity(tableName = "zones")
data class ZoneEntity(
    @PrimaryKey val id: String,
    val name: String,
    val trafficIndex: Int,
    val hasPartialCoverage: Boolean,
    val dataQuality: String
) {
    fun toDomain(luminariaIds: List<String>) = Zone(
        id = id,
        name = name,
        trafficIndex = trafficIndex,
        luminariaIds = luminariaIds,
        hasPartialCoverage = hasPartialCoverage,
        dataQuality = DataQuality.valueOf(dataQuality)
    )

    companion object {
        fun fromDomain(d: Zone) = ZoneEntity(
            id = d.id,
            name = d.name,
            trafficIndex = d.trafficIndex,
            hasPartialCoverage = d.hasPartialCoverage,
            dataQuality = d.dataQuality.name
        )
    }
}
