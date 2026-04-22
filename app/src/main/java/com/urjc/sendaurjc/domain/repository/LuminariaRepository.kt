package com.urjc.sendaurjc.domain.repository

import com.urjc.sendaurjc.domain.model.*
import kotlinx.coroutines.flow.Flow

interface LuminariaRepository {
    // RF1: receive and store operational status
    suspend fun receiveLuminariaStatus(id: String, status: LuminariaStatus): Result<Unit>

    // RF2: receive and store light intensity
    suspend fun receiveLightIntensity(id: String, intensity: Int): Result<Unit>

    // RF3: receive ambient illumination
    suspend fun receiveAmbientIllumination(id: String, lux: Double): Result<Unit>

    // RF4: presence detection events
    suspend fun receivePresenceEvent(event: PresenceEvent): Result<Unit>

    // RF5: aggregated sensor activations
    suspend fun receiveSensorActivation(activation: SensorActivation): Result<Unit>

    // RF7: unique luminaria identification
    suspend fun registerLuminaria(id: String, zoneId: String): Result<Luminaria>

    // RF13: energy data
    suspend fun receiveEnergyData(id: String, powerWatts: Double, cumulativeKwh: Double): Result<Unit>

    // RF12: maintenance state
    suspend fun processMaintenanceState(id: String): Result<Unit>

    fun observeLuminaria(id: String): Flow<Luminaria?>
    suspend fun getLuminaria(id: String): Luminaria?
    suspend fun getLuminariaByZone(zoneId: String): List<Luminaria>

    // RF20: historical data storage
    suspend fun storeHistoricalRecord(luminariaId: String, timestamp: Long): Result<Unit>

    // RF21: internal data query with temporal/spatial filters
    suspend fun queryHistorical(luminariaId: String?, zoneId: String?, from: Long, to: Long): List<Luminaria>
}
