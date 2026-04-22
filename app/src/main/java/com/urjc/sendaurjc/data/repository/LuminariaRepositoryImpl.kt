package com.urjc.sendaurjc.data.repository

import com.urjc.sendaurjc.data.local.dao.LuminariaDao
import com.urjc.sendaurjc.data.local.entity.LuminariaEntity
import com.urjc.sendaurjc.data.local.entity.LuminariaHistoryEntity
import com.urjc.sendaurjc.domain.model.*
import com.urjc.sendaurjc.domain.repository.LuminariaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LuminariaRepositoryImpl @Inject constructor(
    private val dao: LuminariaDao
) : LuminariaRepository {

    override suspend fun receiveLuminariaStatus(id: String, status: LuminariaStatus): Result<Unit> =
        runCatching {
            val existing = dao.getById(id) ?: buildDefaultEntity(id, status)
            dao.upsert(existing.copy(status = status.name, lastUpdated = System.currentTimeMillis()))
        }

    override suspend fun receiveLightIntensity(id: String, intensity: Int): Result<Unit> =
        runCatching {
            require(intensity in 0..100) { "Intensity out of range: $intensity" }
            val existing = dao.getById(id) ?: buildDefaultEntity(id, LuminariaStatus.ENCENDIDA)
            dao.upsert(existing.copy(lightIntensity = intensity, lastUpdated = System.currentTimeMillis()))
        }

    override suspend fun receiveAmbientIllumination(id: String, lux: Double): Result<Unit> =
        runCatching {
            require(lux >= 0) { "Lux cannot be negative: $lux" }
            val existing = dao.getById(id) ?: buildDefaultEntity(id, LuminariaStatus.ENCENDIDA)
            dao.upsert(existing.copy(ambientIllumination = lux, lastUpdated = System.currentTimeMillis()))
        }

    override suspend fun receivePresenceEvent(event: PresenceEvent): Result<Unit> =
        runCatching {
            // Presence events stored in history for zone traffic computation
            val existing = dao.getById(event.sensorId)
            if (existing != null) {
                dao.insertHistory(
                    LuminariaHistoryEntity(
                        luminariaId = event.sensorId,
                        status = existing.status,
                        lightIntensity = existing.lightIntensity,
                        ambientIllumination = existing.ambientIllumination,
                        timestamp = event.timestamp
                    )
                )
            }
        }

    override suspend fun receiveSensorActivation(activation: SensorActivation): Result<Unit> =
        runCatching {
            require(activation.count >= 0) { "Count cannot be negative" }
            require(activation.intervalEnd > activation.intervalStart) { "Invalid interval" }
        }

    override suspend fun registerLuminaria(id: String, zoneId: String): Result<Luminaria> =
        runCatching {
            require(id.isNotBlank()) { "Luminaria ID cannot be empty" }
            val existing = dao.getById(id)
            if (existing != null) return@runCatching existing.toDomain()
            val entity = buildDefaultEntity(id, LuminariaStatus.APAGADA).copy(zoneId = zoneId)
            dao.upsert(entity)
            entity.toDomain()
        }

    override suspend fun receiveEnergyData(id: String, powerWatts: Double, cumulativeKwh: Double): Result<Unit> =
        runCatching {
            require(powerWatts >= 0) { "Power cannot be negative" }
            require(cumulativeKwh >= 0) { "Consumption cannot be negative" }
            val existing = dao.getById(id) ?: buildDefaultEntity(id, LuminariaStatus.ENCENDIDA)
            dao.upsert(existing.copy(powerWatts = powerWatts, cumulativeConsumption = cumulativeKwh))
        }

    override suspend fun processMaintenanceState(id: String): Result<Unit> =
        receiveLuminariaStatus(id, LuminariaStatus.EN_MANTENIMIENTO)

    override fun observeLuminaria(id: String): Flow<Luminaria?> =
        dao.observe(id).map { it?.toDomain() }

    override suspend fun getLuminaria(id: String): Luminaria? =
        dao.getById(id)?.toDomain()

    override suspend fun getLuminariaByZone(zoneId: String): List<Luminaria> =
        dao.getByZone(zoneId).map { it.toDomain() }

    override suspend fun storeHistoricalRecord(luminariaId: String, timestamp: Long): Result<Unit> =
        runCatching {
            val existing = dao.getById(luminariaId) ?: return@runCatching
            dao.insertHistory(
                LuminariaHistoryEntity(
                    luminariaId = luminariaId,
                    status = existing.status,
                    lightIntensity = existing.lightIntensity,
                    ambientIllumination = existing.ambientIllumination,
                    timestamp = timestamp
                )
            )
        }

    override suspend fun queryHistorical(luminariaId: String?, zoneId: String?, from: Long, to: Long): List<Luminaria> {
        val history = dao.queryHistory(luminariaId, from, to)
        return history.mapNotNull { dao.getById(it.luminariaId)?.toDomain() }
    }

    private fun buildDefaultEntity(id: String, status: LuminariaStatus) = LuminariaEntity(
        id = id,
        zoneId = "",
        status = status.name,
        lightIntensity = 0,
        ambientIllumination = 0.0,
        powerWatts = 0.0,
        cumulativeConsumption = 0.0,
        lastUpdated = System.currentTimeMillis()
    )
}
