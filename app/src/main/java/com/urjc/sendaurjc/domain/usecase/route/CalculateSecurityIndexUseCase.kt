package com.urjc.sendaurjc.domain.usecase.route

import com.urjc.sendaurjc.domain.model.CalibrationPreferences
import com.urjc.sendaurjc.domain.model.DataQuality
import com.urjc.sendaurjc.domain.model.RouteSegment
import javax.inject.Inject

/**
 * Implements the logic for RF31, RF32, RF33, RF34, RF35, RF23
 * Also integrates RF73, RF74, RF75 (Calibration weights)
 */
class CalculateSecurityIndexUseCase @Inject constructor() {

    operator fun invoke(segments: List<RouteSegment>, prefs: CalibrationPreferences = CalibrationPreferences("default")): Double {
        if (segments.isEmpty()) return 0.0

        val totalScore = segments.sumOf { segment ->
            var score = 50.0 // Base score

            // RF33, RF73: Prioridad de calles con alumbrado activo
            if (segment.hasActiveLight) {
                score += (30.0 * prefs.weightLighting)
            }

            // RF34, RF74: Prioridad de zonas con mayor afluencia (Traffic index is 0-100)
            score += ((segment.trafficIndex / 100.0) * 15.0 * prefs.weightTraffic)

            // RF35, RF75: Penalización de zonas con obstáculos
            if (segment.hasObstacles) {
                val penalty = if (prefs.avoidObstacles) 40.0 else 10.0
                score -= (penalty * prefs.weightEnvironment)
            }

            // RF23: Clasificación de la calidad de los datos
            if (segment.dataQuality == DataQuality.LOW.name) {
                score -= 10.0
            }

            // RF31: Ensures index is between 0 and 100
            score.coerceIn(0.0, 100.0)
        }

        return totalScore / segments.size
    }
}
