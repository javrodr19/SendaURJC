package com.urjc.sendaurjc.util

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.urjc.sendaurjc.BuildConfig
import com.urjc.sendaurjc.data.remote.api.LumenSmartApi
import com.urjc.sendaurjc.data.remote.dto.OAuthRequestDto
import com.urjc.sendaurjc.domain.model.*
import com.urjc.sendaurjc.domain.repository.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

// RF11: periodic REST API polling; RF19: process data by update frequency
@HiltWorker
class LumenSmartSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val api: LumenSmartApi,
    private val luminariaRepository: LuminariaRepository,
    private val zoneRepository: ZoneRepository,
    private val alertRepository: AlertRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val tokenResult = api.getOAuthToken(
                OAuthRequestDto(
                    clientId = "senda_urjc",
                    clientSecret = BuildConfig.WEBHOOK_SECRET
                )
            )
            val token = tokenResult.body()?.accessToken ?: return Result.retry()
            val bearer = "Bearer $token"

            // Sync luminarias
            api.getAllLuminarias(bearer).body()?.forEach { dto ->
                luminariaRepository.registerLuminaria(dto.id, dto.zoneId)
                luminariaRepository.receiveLuminariaStatus(dto.id, LuminariaStatus.valueOf(dto.status))
                luminariaRepository.receiveLightIntensity(dto.id, dto.intensity)
                luminariaRepository.receiveAmbientIllumination(dto.id, dto.lux)
                luminariaRepository.receiveEnergyData(dto.id, dto.powerWatts, dto.cumulativeKwh)
                luminariaRepository.storeHistoricalRecord(dto.id, dto.timestamp)  // RF20
            }

            // Sync zones traffic
            api.getAllZones(bearer).body()?.forEach { dto ->
                zoneRepository.receiveTrafficIndex(dto.zoneId, dto.trafficIndex)
            }

            // Sync critical alerts (RF10, RF18)
            val since = System.currentTimeMillis() - 5 * 60 * 1000  // last 5 minutes
            api.getCriticalAlerts(bearer, since).body()?.forEach { dto ->
                val alertResult = alertRepository.receiveCriticalAlert(
                    type = AlertType.valueOf(dto.type),
                    luminariaId = dto.luminariaId,
                    zoneId = dto.zoneId,
                    timestamp = dto.timestamp
                )
                alertResult.getOrNull()?.let { alert ->
                    alertRepository.processCriticalAlert(alert)  // RF18: <10s
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
