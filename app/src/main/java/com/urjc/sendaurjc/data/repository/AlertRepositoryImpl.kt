package com.urjc.sendaurjc.data.repository

import com.urjc.sendaurjc.data.local.dao.AlertDao
import com.urjc.sendaurjc.data.local.entity.AlertEntity
import com.urjc.sendaurjc.domain.model.AlertType
import com.urjc.sendaurjc.domain.model.CriticalAlert
import com.urjc.sendaurjc.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepositoryImpl @Inject constructor(
    private val dao: AlertDao
) : AlertRepository {

    override suspend fun receiveCriticalAlert(
        type: AlertType,
        luminariaId: String?,
        zoneId: String?,
        timestamp: Long
    ): Result<CriticalAlert> = runCatching {
        val alert = CriticalAlert(
            id = UUID.randomUUID().toString(),
            type = type,
            luminariaId = luminariaId,
            zoneId = zoneId,
            timestamp = timestamp,
            processed = false
        )
        val inserted = dao.insert(AlertEntity.fromDomain(alert))
        if (inserted == -1L) error("Duplicate alert")
        alert
    }

    // RF18: must process within 10 seconds
    override suspend fun processCriticalAlert(alert: CriticalAlert): Result<Unit> =
        runCatching {
            val receiveTime = System.currentTimeMillis()
            // Processing logic happens here; latency check is monitored by caller
            dao.markProcessed(alert.id)
            val elapsed = System.currentTimeMillis() - receiveTime
            check(elapsed < 10_000) { "Processing exceeded 10s: ${elapsed}ms" }
        }

    override fun observeUnprocessedAlerts(): Flow<List<CriticalAlert>> =
        dao.observeUnprocessed().map { list -> list.map { it.toDomain() } }

    override suspend fun markAlertProcessed(alertId: String) =
        dao.markProcessed(alertId)

    override suspend fun getAllAlerts(): List<CriticalAlert> =
        dao.getAll().map { it.toDomain() }
}
