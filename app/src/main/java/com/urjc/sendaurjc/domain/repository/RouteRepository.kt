package com.urjc.sendaurjc.domain.repository

import com.urjc.sendaurjc.domain.model.GeoPoint
import com.urjc.sendaurjc.domain.model.NavigationInstruction
import com.urjc.sendaurjc.domain.model.Route
import com.urjc.sendaurjc.domain.model.RouteHistory
import kotlinx.coroutines.flow.Flow

interface RouteRepository {
    // RF29, RF30: compute multiple routes
    suspend fun calculateRoutes(origin: GeoPoint, destination: GeoPoint): Result<List<Route>>

    // RF24: recalculate dynamically
    suspend fun recalculateRoute(activeRoute: Route): Result<Route>

    // RF25: navigation instructions
    fun getNavigationInstructions(route: Route): List<NavigationInstruction>

    // RF38: select active route
    suspend fun setActiveRoute(route: Route)
    fun observeActiveRoute(): Flow<Route?>
    suspend fun clearActiveRoute()

    // RF39: real-time location sharing to trusted contact
    suspend fun shareLocationToTrustedContact(location: GeoPoint, contactId: String): Result<Unit>

    // RF41: change route before start
    suspend fun changeRoute(newRoute: Route): Result<Unit>

    // RF46: Almacenamiento del historial de rutas
    suspend fun saveRouteHistory(history: RouteHistory): Result<Unit>
    
    // RF70: Consulta del historial de rutas del usuario
    suspend fun getRouteHistory(userId: String): List<RouteHistory>
}
