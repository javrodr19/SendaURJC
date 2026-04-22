package com.urjc.sendaurjc.domain.repository

import com.urjc.sendaurjc.domain.model.DataQuality
import com.urjc.sendaurjc.domain.model.Zone
import kotlinx.coroutines.flow.Flow

interface ZoneRepository {
    // RF8: register zone
    suspend fun registerZone(id: String, name: String): Result<Zone>

    // RF6: traffic index
    suspend fun receiveTrafficIndex(zoneId: String, index: Int): Result<Unit>

    // RF9: associate luminaria to zone
    suspend fun associateLuminaria(luminariaId: String, zoneId: String): Result<Unit>

    // RF22: mark segment with partial information
    suspend fun markPartialCoverage(zoneId: String, isPartial: Boolean): Result<Unit>

    // RF23: classify data quality
    suspend fun classifyDataQuality(zoneId: String, quality: DataQuality): Result<Unit>

    suspend fun getZone(id: String): Zone?
    fun observeZone(id: String): Flow<Zone?>
    suspend fun getAllZones(): List<Zone>
}
