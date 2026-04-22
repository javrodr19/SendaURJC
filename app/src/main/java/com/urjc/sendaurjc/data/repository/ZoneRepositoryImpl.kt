package com.urjc.sendaurjc.data.repository

import com.urjc.sendaurjc.data.local.dao.LuminariaDao
import com.urjc.sendaurjc.data.local.dao.ZoneDao
import com.urjc.sendaurjc.data.local.entity.ZoneEntity
import com.urjc.sendaurjc.domain.model.DataQuality
import com.urjc.sendaurjc.domain.model.Zone
import com.urjc.sendaurjc.domain.repository.ZoneRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZoneRepositoryImpl @Inject constructor(
    private val zoneDao: ZoneDao,
    private val luminariaDao: LuminariaDao
) : ZoneRepository {

    override suspend fun registerZone(id: String, name: String): Result<Zone> =
        runCatching {
            require(id.isNotBlank()) { "Zone ID cannot be empty" }
            val existing = zoneDao.getById(id)
            if (existing != null) return@runCatching existing.toDomain(getLuminariaIds(id))
            val entity = ZoneEntity(
                id = id,
                name = name,
                trafficIndex = 0,
                hasPartialCoverage = false,
                dataQuality = DataQuality.HIGH.name
            )
            zoneDao.upsert(entity)
            entity.toDomain(emptyList())
        }

    override suspend fun receiveTrafficIndex(zoneId: String, index: Int): Result<Unit> =
        runCatching {
            require(index in 0..100) { "Traffic index out of range: $index" }
            requireNotNull(zoneDao.getById(zoneId)) { "Zone not found: $zoneId" }
            zoneDao.updateTrafficIndex(zoneId, index)
        }

    override suspend fun associateLuminaria(luminariaId: String, zoneId: String): Result<Unit> =
        runCatching {
            val luminaria = luminariaDao.getById(luminariaId) ?: error("Luminaria not found: $luminariaId")
            requireNotNull(zoneDao.getById(zoneId)) { "Zone not found: $zoneId" }
            luminariaDao.upsert(luminaria.copy(zoneId = zoneId))
        }

    override suspend fun markPartialCoverage(zoneId: String, isPartial: Boolean): Result<Unit> =
        runCatching {
            requireNotNull(zoneDao.getById(zoneId)) { "Zone not found: $zoneId" }
            zoneDao.updatePartialCoverage(zoneId, isPartial)
        }

    override suspend fun classifyDataQuality(zoneId: String, quality: DataQuality): Result<Unit> =
        runCatching {
            requireNotNull(zoneDao.getById(zoneId)) { "Zone not found: $zoneId" }
            zoneDao.updateDataQuality(zoneId, quality.name)
        }

    override suspend fun getZone(id: String): Zone? {
        val entity = zoneDao.getById(id) ?: return null
        return entity.toDomain(getLuminariaIds(id))
    }

    override fun observeZone(id: String): Flow<Zone?> =
        zoneDao.observe(id).map { entity ->
            entity?.toDomain(getLuminariaIds(id))
        }

    override suspend fun getAllZones(): List<Zone> =
        zoneDao.getAll().map { entity -> entity.toDomain(getLuminariaIds(entity.id)) }

    private suspend fun getLuminariaIds(zoneId: String): List<String> =
        luminariaDao.getByZone(zoneId).map { it.id }
}
