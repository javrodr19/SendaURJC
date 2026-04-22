package com.urjc.sendaurjc.domain.usecase.route

import com.urjc.sendaurjc.domain.model.CalibrationPreferences
import com.urjc.sendaurjc.domain.repository.UserRepository
import javax.inject.Inject

/**
 * RF72: Configuración de calibraciones de rutas
 * RF73: Calibración de condiciones de iluminación
 * RF74: Calibración de condiciones de afluencia
 * RF75: Calibración del estado entorno
 */
class CalibrationUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun getPreferences(): Result<CalibrationPreferences> {
        val user = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("User not logged in"))

        return Result.success(userRepository.getCalibrationPreferences(user.id))
    }

    suspend fun updatePreferences(
        weightLighting: Float? = null,
        weightTraffic: Float? = null,
        weightEnvironment: Float? = null,
        preferSafest: Boolean? = null,
        avoidObstacles: Boolean? = null
    ): Result<Unit> {
        val user = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("User not logged in"))

        val currentPrefs = userRepository.getCalibrationPreferences(user.id)
        
        val updatedPrefs = currentPrefs.copy(
            weightLighting = weightLighting ?: currentPrefs.weightLighting,
            weightTraffic = weightTraffic ?: currentPrefs.weightTraffic,
            weightEnvironment = weightEnvironment ?: currentPrefs.weightEnvironment,
            preferSafest = preferSafest ?: currentPrefs.preferSafest,
            avoidObstacles = avoidObstacles ?: currentPrefs.avoidObstacles
        )

        return userRepository.saveCalibrationPreferences(updatedPrefs)
    }
}
