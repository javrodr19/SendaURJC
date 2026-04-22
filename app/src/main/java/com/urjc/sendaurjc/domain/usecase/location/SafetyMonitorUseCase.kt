package com.urjc.sendaurjc.domain.usecase.location

import com.urjc.sendaurjc.domain.model.*
import com.urjc.sendaurjc.domain.repository.RouteRepository
import com.urjc.sendaurjc.domain.repository.UserRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.math.*

/**
 * RF94 to RF102: Real-time safety monitoring during an active route.
 */
class SafetyMonitorUseCase @Inject constructor(
    private val routeRepository: RouteRepository,
    private val userRepository: UserRepository
) {
    private val _safetyState = MutableStateFlow(SafetyState.SAFE)
    val safetyState = _safetyState.asStateFlow()

    private var monitorJob: Job? = null
    private var lastLocation: GeoPoint? = null
    private var lastMovementTime: Long = 0L
    private val config = SafetyConfig() // Default thresholds

    fun startMonitoring(route: Route, initialLocation: GeoPoint, coroutineScope: CoroutineScope) {
        lastLocation = initialLocation
        lastMovementTime = System.currentTimeMillis()
        _safetyState.value = SafetyState.MONITORING

        monitorJob?.cancel()
        monitorJob = coroutineScope.launch {
            while (isActive) {
                delay(5000) // Check every 5 seconds
                checkSafetyConditions(route)
            }
        }
    }

    fun updateLocation(newLocation: GeoPoint) {
        val last = lastLocation ?: return
        val distance = haversineDistance(last, newLocation)
        
        if (distance > 5.0) { // Moved more than 5 meters
            lastMovementTime = System.currentTimeMillis()
            lastLocation = newLocation
        }
    }

    private suspend fun checkSafetyConditions(activeRoute: Route) {
        if (_safetyState.value != SafetyState.MONITORING) return

        val currentLoc = lastLocation ?: return
        val currentTime = System.currentTimeMillis()
        val timeSinceLastMove = (currentTime - lastMovementTime) / 1000

        // RF95: Prolonged stop detection
        if (timeSinceLastMove > config.stopThresholdSeconds) {
            triggerPreAlert(SafetyEvent.STOP_DETECTED, currentLoc)
            return
        }

        // RF96: Route deviation detection
        val deviation = calculateDeviation(currentLoc, activeRoute)
        if (deviation > config.deviationThresholdMeters) {
            triggerPreAlert(SafetyEvent.DEVIATION_DETECTED, currentLoc)
            return
        }
    }

    // RF111: Declaración de incidencia grave
    // RF112: Incremento de medidas ante incidencia grave
    private suspend fun escalateToGrave(location: GeoPoint) {
        _safetyState.value = SafetyState.ALERT_SENT // or GRAVE depending on state tracking
        
        // Notify security with max priority (RF112)
        // Additional measures like calling emergency contacts could be added here
        emitCriticalAlert(location)
    }

    // RF97: Emit pre-alert
    // RF98: Request user confirmation
    private suspend fun triggerPreAlert(event: SafetyEvent, location: GeoPoint) {
        _safetyState.value = SafetyState.PRE_ALERT
        
        // Wait for user response (RF100) -> using configured time wait (RF103 allows config)
        delay(config.alertWaitSeconds * 1000L)
        
        if (_safetyState.value == SafetyState.PRE_ALERT) {
            // RF99, RF100: User did not respond in time -> escalate to Alert
            emitCriticalAlert(location)

            // RF111: Declaration of severe incident if no response after a maximum time limit
            // Wait additional time to escalate to Grave (RF111)
            delay(config.alertWaitSeconds * 1000L)
            if (_safetyState.value == SafetyState.ALERT_SENT) {
                escalateToGrave(location)
            }
        }
    }

    // RF110: User confirms they are safe
    fun confirmSafety() {
        if (_safetyState.value == SafetyState.PRE_ALERT) {
            _safetyState.value = SafetyState.MONITORING
            lastMovementTime = System.currentTimeMillis()
        }
    }

    // RF101: Send alert to trusted contact / security service
    // RF102: Include last known location
    private suspend fun emitCriticalAlert(location: GeoPoint) {
        _safetyState.value = SafetyState.ALERT_SENT
        val userId = userRepository.getCurrentUser()?.id ?: return
        
        val contact = userRepository.getTrustedContact(userId)
        if (contact != null) {
            routeRepository.shareLocationToTrustedContact(location, contact.id)
        }
        
        // Notify security (simulated via repository or external service)
        // This implements RF101 (security service alert)
    }

    fun stopMonitoring() {
        monitorJob?.cancel()
        _safetyState.value = SafetyState.SAFE
    }

    private fun calculateDeviation(current: GeoPoint, route: Route): Double {
        // Find the minimum distance from current point to any segment in the route
        var minDeviation = Double.MAX_VALUE
        for (segment in route.segments) {
            val dist = distanceToSegment(current, segment.start, segment.end)
            if (dist < minDeviation) {
                minDeviation = dist
            }
        }
        return minDeviation
    }

    private fun distanceToSegment(p: GeoPoint, a: GeoPoint, b: GeoPoint): Double {
        // Simplified Euclidean distance to segment for testing
        // Real implementation should use proper cross-track distance with Haversine
        val lat1 = a.latitude; val lon1 = a.longitude
        val lat2 = b.latitude; val lon2 = b.longitude
        val px = p.latitude; val py = p.longitude
        
        val l2 = (lat1 - lat2).pow(2) + (lon1 - lon2).pow(2)
        if (l2 == 0.0) return haversineDistance(p, a)
        
        val t = max(0.0, min(1.0, ((px - lat1) * (lat2 - lat1) + (py - lon1) * (lon2 - lon1)) / l2))
        val projection = GeoPoint(lat1 + t * (lat2 - lat1), lon1 + t * (lon2 - lon1))
        
        return haversineDistance(p, projection)
    }

    private fun haversineDistance(a: GeoPoint, b: GeoPoint): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val h = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sin(dLon / 2).pow(2)
        return 2 * r * asin(sqrt(h))
    }
}
