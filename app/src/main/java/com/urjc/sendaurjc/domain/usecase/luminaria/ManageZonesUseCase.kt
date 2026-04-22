package com.urjc.sendaurjc.domain.usecase.luminaria

import com.urjc.sendaurjc.domain.model.DataQuality
import com.urjc.sendaurjc.domain.model.Zone
import com.urjc.sendaurjc.domain.repository.ZoneRepository
import javax.inject.Inject

/**
 * RF6, RF8, RF9, RF22, RF23: Logic for managing campus zones and data quality.
 */
class ManageZonesUseCase @Inject constructor(
    private val zoneRepository: ZoneRepository
) {

    // RF8: Identification of campus zones
    suspend fun createZone(id: String, name: String): Result<Zone> {
        return zoneRepository.registerZone(id, name)
    }

    // RF6: Traffic index reception (0-100)
    suspend fun updateTrafficIndex(zoneId: String, index: Int): Result<Unit> {
        if (index !in 0..100) {
            return Result.failure(IllegalArgumentException("Traffic index must be 0-100"))
        }
        return zoneRepository.receiveTrafficIndex(zoneId, index)
    }

    // RF9: Association of luminarias to zones
    suspend fun addLuminariaToZone(luminariaId: String, zoneId: String): Result<Unit> {
        return zoneRepository.associateLuminaria(luminariaId, zoneId)
    }

    // RF22: Marked with partial info
    suspend fun setPartialCoverage(zoneId: String, isPartial: Boolean): Result<Unit> {
        return zoneRepository.markPartialCoverage(zoneId, isPartial)
    }

    // RF23: Classification of data quality
    suspend fun setQuality(zoneId: String, quality: DataQuality): Result<Unit> {
        return zoneRepository.classifyDataQuality(zoneId, quality)
    }
}
