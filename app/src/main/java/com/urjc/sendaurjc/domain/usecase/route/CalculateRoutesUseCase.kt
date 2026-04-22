package com.urjc.sendaurjc.domain.usecase.route

import com.urjc.sendaurjc.domain.model.CalibrationPreferences
import com.urjc.sendaurjc.domain.model.GeoPoint
import com.urjc.sendaurjc.domain.model.Route
import com.urjc.sendaurjc.domain.repository.RouteRepository
import com.urjc.sendaurjc.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Implements RF29, RF30, RF36, RF37: Calculate routes, assess their security index,
 * and identify the safest one.
 */
class CalculateRoutesUseCase @Inject constructor(
    private val routeRepository: RouteRepository,
    private val calculateSecurityIndexUseCase: CalculateSecurityIndexUseCase,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(origin: GeoPoint, destination: GeoPoint): Result<List<Route>> {
        val result = routeRepository.calculateRoutes(origin, destination)
        
        return if (result.isSuccess) {
            val routes = result.getOrThrow()
            
            val userId = userRepository.getCurrentUser()?.id
            val prefs = if (userId != null) {
                userRepository.getCalibrationPreferences(userId)
            } else {
                CalibrationPreferences("default")
            }
            
            // RF31, RF32: Calculate security index for each route
            val routesWithSecurity = routes.map { route ->
                val index = calculateSecurityIndexUseCase(route.segments, prefs)
                route.copy(securityIndex = index)
            }

            if (routesWithSecurity.isEmpty()) {
                return Result.failure(Exception("No routes could be calculated"))
            }

            // RF36: Identify the safest route
            val maxIndex = routesWithSecurity.maxOf { it.securityIndex }
            
            // RF37: Present sorted alternative routes based on security index
            val finalRoutes = routesWithSecurity.map { route ->
                if (route.securityIndex == maxIndex) {
                    route.copy(isSafest = true)
                } else {
                    route.copy(isSafest = false)
                }
            }.sortedByDescending { it.securityIndex }

            Result.success(finalRoutes)
        } else {
            result
        }
    }
}
