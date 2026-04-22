package com.urjc.sendaurjc.presentation.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urjc.sendaurjc.domain.model.*
import com.urjc.sendaurjc.domain.repository.RouteRepository
import com.urjc.sendaurjc.domain.usecase.location.SafetyMonitorUseCase
import com.urjc.sendaurjc.domain.usecase.route.CalculateRoutesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val calculateRoutesUseCase: CalculateRoutesUseCase,
    private val safetyMonitorUseCase: SafetyMonitorUseCase,
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RouteUiState>(RouteUiState.Idle)
    val uiState: StateFlow<RouteUiState> = _uiState.asStateFlow()

    private val _availableRoutes = MutableStateFlow<List<Route>>(emptyList())
    val availableRoutes: StateFlow<List<Route>> = _availableRoutes.asStateFlow()

    val safetyState: StateFlow<SafetyState> = safetyMonitorUseCase.safetyState

    fun findRoutes(origin: GeoPoint, destination: GeoPoint) {
        _uiState.value = RouteUiState.Loading
        viewModelScope.launch {
            calculateRoutesUseCase(origin, destination)
                .onSuccess { routes ->
                    _availableRoutes.value = routes
                    _uiState.value = RouteUiState.RoutesCalculated(routes)
                }
                .onFailure { error ->
                    _uiState.value = RouteUiState.Error(error.message ?: "Error al calcular rutas")
                }
        }
    }

    fun startNavigation(route: Route) {
        _uiState.value = RouteUiState.Navigating(route)
        viewModelScope.launch {
            routeRepository.setActiveRoute(route)
            safetyMonitorUseCase.startMonitoring(route, route.origin, viewModelScope)
        }
    }

    fun updateCurrentLocation(location: GeoPoint) {
        safetyMonitorUseCase.updateLocation(location)
    }

    fun confirmUserIsSafe() {
        safetyMonitorUseCase.confirmSafety()
    }

    fun finishRoute() {
        safetyMonitorUseCase.stopMonitoring()
        _uiState.value = RouteUiState.Completed
    }

    sealed class RouteUiState {
        object Idle : RouteUiState()
        object Loading : RouteUiState()
        data class RoutesCalculated(val routes: List<Route>) : RouteUiState()
        data class Navigating(val route: Route) : RouteUiState()
        object Completed : RouteUiState()
        data class Error(val message: String) : RouteUiState()
    }
}
