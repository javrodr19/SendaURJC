package com.urjc.sendaurjc.data.repository

import com.urjc.sendaurjc.domain.model.*
import com.urjc.sendaurjc.domain.repository.LuminariaRepository
import com.urjc.sendaurjc.domain.repository.RouteRepository
import com.urjc.sendaurjc.domain.repository.UserRepository
import com.urjc.sendaurjc.domain.repository.ZoneRepository
import com.urjc.sendaurjc.domain.usecase.route.CalculateSecurityIndexUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class RouteRepositoryImpl @Inject constructor(
    private val luminariaRepository: LuminariaRepository,
    private val zoneRepository: ZoneRepository,
    private val calculateSecurityIndexUseCase: CalculateSecurityIndexUseCase,
    private val userRepository: UserRepository
) : RouteRepository {

    private val _activeRoute = MutableStateFlow<Route?>(null)

    // RF30: calculate multiple routes; RF31+32+33+34+35+36+37: security index
    override suspend fun calculateRoutes(origin: GeoPoint, destination: GeoPoint): Result<List<Route>> =
        runCatching {
            val zones = zoneRepository.getAllZones()
            val userId = userRepository.getCurrentUser()?.id
            val prefs = if (userId != null) {
                userRepository.getCalibrationPreferences(userId)
            } else {
                CalibrationPreferences("default")
            }
            val routes = generateCandidateRoutes(origin, destination, zones, prefs)
            routes.sortedByDescending { it.securityIndex }
        }

    // RF24: recalculate dynamically when illumination/traffic data changes
    override suspend fun recalculateRoute(activeRoute: Route): Result<Route> =
        runCatching {
            val updatedSegments = activeRoute.segments.map { segment ->
                val zone = zoneRepository.getZone(segment.zoneId)
                val luminarias = luminariaRepository.getLuminariaByZone(segment.zoneId)
                segment.copy(
                    hasActiveLight = luminarias.any { it.status == LuminariaStatus.ENCENDIDA },
                    trafficIndex = zone?.trafficIndex ?: segment.trafficIndex
                )
            }
            
            val userId = userRepository.getCurrentUser()?.id
            val prefs = if (userId != null) {
                userRepository.getCalibrationPreferences(userId)
            } else {
                CalibrationPreferences("default")
            }
            
            val updatedRoute = activeRoute.copy(
                segments = updatedSegments,
                securityIndex = calculateSecurityIndexUseCase(updatedSegments, prefs)
            )
            _activeRoute.value = updatedRoute
            updatedRoute
        }

    override fun getNavigationInstructions(route: Route): List<NavigationInstruction> {
        return route.segments.mapIndexed { index, segment ->
            val bearing = computeBearing(segment.start, segment.end)
            val direction = bearingToDirection(bearing)
            NavigationInstruction(
                text = "En ${haversineDistance(segment.start, segment.end).roundToInt()}m, $direction",
                distanceMeters = haversineDistance(segment.start, segment.end),
                point = segment.end
            )
        }
    }

    override suspend fun setActiveRoute(route: Route) {
        _activeRoute.value = route
    }

    override fun observeActiveRoute(): Flow<Route?> = _activeRoute.asStateFlow()

    override suspend fun clearActiveRoute() {
        _activeRoute.value = null
    }

    override suspend fun shareLocationToTrustedContact(location: GeoPoint, contactId: String): Result<Unit> =
        runCatching {
            // RF39: actual sending is done via UrjcApi – here we just validate inputs
            require(contactId.isNotBlank()) { "Contact ID cannot be empty" }
        }

    override suspend fun changeRoute(newRoute: Route): Result<Unit> = runCatching {
        _activeRoute.value = newRoute
    }

    private fun generateCandidateRoutes(
        origin: GeoPoint,
        destination: GeoPoint,
        zones: List<Zone>,
        prefs: CalibrationPreferences
    ): List<Route> {
        val baseSegment = RouteSegment(
            start = origin,
            end = destination,
            zoneId = zones.firstOrNull()?.id ?: "unknown",
            hasActiveLight = true,
            trafficIndex = zones.firstOrNull()?.trafficIndex ?: 50,
            hasObstacles = false,
            dataQuality = DataQuality.HIGH.name
        )

        val route1 = buildRoute(origin, destination, listOf(baseSegment), prefs)
        val route2Segment = baseSegment.copy(
            hasActiveLight = false,
            trafficIndex = 80,
            hasObstacles = false
        )
        val route2 = buildRoute(origin, destination, listOf(route2Segment), prefs)

        return listOf(route1, route2).sortedByDescending { it.securityIndex }.let { sorted ->
            sorted.mapIndexed { i, r -> r.copy(isSafest = i == 0) }
        }
    }

    private fun buildRoute(
        origin: GeoPoint, 
        destination: GeoPoint, 
        segments: List<RouteSegment>, 
        prefs: CalibrationPreferences
    ): Route {
        val distanceM = haversineDistance(origin, destination)
        return Route(
            id = UUID.randomUUID().toString(),
            origin = origin,
            destination = destination,
            segments = segments,
            securityIndex = calculateSecurityIndexUseCase(segments, prefs),
            estimatedMinutes = (distanceM / 80.0).roundToInt()  // ~80m/min walking
        )
    }

    private fun haversineDistance(a: GeoPoint, b: GeoPoint): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val h = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sin(dLon / 2).pow(2)
        return 2 * r * asin(sqrt(h))
    }

    private fun computeBearing(from: GeoPoint, to: GeoPoint): Double {
        val dLon = Math.toRadians(to.longitude - from.longitude)
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }

    private fun bearingToDirection(bearing: Double) = when {
        bearing < 22.5 || bearing >= 337.5 -> "gire al norte"
        bearing < 67.5 -> "gire al noreste"
        bearing < 112.5 -> "gire al este"
        bearing < 157.5 -> "gire al sureste"
        bearing < 202.5 -> "gire al sur"
        bearing < 247.5 -> "gire al suroeste"
        bearing < 292.5 -> "gire al oeste"
        else -> "gire al noroeste"
    }
}

private fun Double.roundToInt(): Int = kotlin.math.roundToInt(this)
