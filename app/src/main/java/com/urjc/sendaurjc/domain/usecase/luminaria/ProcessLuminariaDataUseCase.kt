package com.urjc.sendaurjc.domain.usecase.luminaria

import com.urjc.sendaurjc.domain.model.LuminariaStatus
import com.urjc.sendaurjc.domain.model.PresenceEvent
import com.urjc.sendaurjc.domain.model.SensorActivation
import com.urjc.sendaurjc.domain.repository.LuminariaRepository
import javax.inject.Inject

/**
 * Encapsulates the logic for processing data received from LumenSmart (RF1-RF5, RF12, RF13)
 */
class ProcessLuminariaDataUseCase @Inject constructor(
    private val luminariaRepository: LuminariaRepository
) {

    // RF1, RF12: Status and Maintenance
    suspend fun updateStatus(id: String, status: LuminariaStatus): Result<Unit> {
        return luminariaRepository.receiveLuminariaStatus(id, status)
    }

    // RF2: Light Intensity (0-100)
    suspend fun updateIntensity(id: String, intensity: Int): Result<Unit> {
        if (intensity !in 0..100) {
            return Result.failure(IllegalArgumentException("Intensity must be between 0 and 100"))
        }
        return luminariaRepository.receiveLightIntensity(id, intensity)
    }

    // RF3: Ambient Illumination (lux)
    suspend fun updateAmbientIllumination(id: String, lux: Double): Result<Unit> {
        if (lux < 0) {
            return Result.failure(IllegalArgumentException("Illumination cannot be negative"))
        }
        return luminariaRepository.receiveAmbientIllumination(id, lux)
    }

    // RF4: Presence Detection
    suspend fun processPresenceEvent(event: PresenceEvent): Result<Unit> {
        return luminariaRepository.receivePresenceEvent(event)
    }

    // RF5: Aggregated activations
    suspend fun processSensorActivation(activation: SensorActivation): Result<Unit> {
        if (activation.count < 0) {
            return Result.failure(IllegalArgumentException("Activation count cannot be negative"))
        }
        return luminariaRepository.receiveSensorActivation(activation)
    }

    // RF13: Energy data
    suspend fun processEnergyData(id: String, powerWatts: Double, cumulativeKwh: Double): Result<Unit> {
        if (powerWatts < 0 || cumulativeKwh < 0) {
            return Result.failure(IllegalArgumentException("Energy data cannot be negative"))
        }
        return luminariaRepository.receiveEnergyData(id, powerWatts, cumulativeKwh)
    }
}
