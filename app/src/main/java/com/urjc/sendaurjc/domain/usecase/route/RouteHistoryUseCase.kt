package com.urjc.sendaurjc.domain.usecase.route

import com.urjc.sendaurjc.domain.model.RouteHistory
import com.urjc.sendaurjc.domain.repository.RouteRepository
import com.urjc.sendaurjc.domain.repository.UserRepository
import javax.inject.Inject

/**
 * RF46: Almacenamiento del historial de rutas
 * RF70: Consulta del historial de rutas del usuario
 * RF71: Visualización de estadísticas del usuario
 */
class RouteHistoryUseCase @Inject constructor(
    private val routeRepository: RouteRepository,
    private val userRepository: UserRepository
) {
    suspend fun saveRouteToHistory(history: RouteHistory): Result<Unit> {
        return routeRepository.saveRouteHistory(history)
    }

    suspend fun getUserRouteHistory(): Result<List<RouteHistory>> {
        val user = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("User not authenticated"))
        
        return Result.success(routeRepository.getRouteHistory(user.id))
    }

    suspend fun getUserStatistics(): Result<UserStatistics> {
        val user = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("User not authenticated"))

        val history = routeRepository.getRouteHistory(user.id)
        
        val totalDistance = history.sumOf { it.distanceMeters }
        val avgSecurityIndex = if (history.isNotEmpty()) {
            history.sumOf { it.securityIndex } / history.size
        } else 0.0

        return Result.success(
            UserStatistics(
                totalRoutes = history.size,
                totalDistanceMeters = totalDistance,
                averageSecurityIndex = avgSecurityIndex,
                routesWithCompanion = history.count { it.hadCompanion }
            )
        )
    }
}

data class UserStatistics(
    val totalRoutes: Int,
    val totalDistanceMeters: Double,
    val averageSecurityIndex: Double,
    val routesWithCompanion: Int
)
