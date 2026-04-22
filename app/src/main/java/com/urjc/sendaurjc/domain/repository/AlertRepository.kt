package com.urjc.sendaurjc.domain.repository

import com.urjc.sendaurjc.domain.model.AlertType
import com.urjc.sendaurjc.domain.model.CriticalAlert
import kotlinx.coroutines.flow.Flow

interface AlertRepository {
    // RF10: receive and register critical alerts
    suspend fun receiveCriticalAlert(
        type: AlertType,
        luminariaId: String?,
        zoneId: String?,
        timestamp: Long
    ): Result<CriticalAlert>

    // RF18: must be processed within 10 seconds
    suspend fun processCriticalAlert(alert: CriticalAlert): Result<Unit>

    fun observeUnprocessedAlerts(): Flow<List<CriticalAlert>>
    suspend fun markAlertProcessed(alertId: String)
    suspend fun getAllAlerts(): List<CriticalAlert>
}
