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

sealed class RouteUiState {
    object Idle : RouteUiState()
    object Loading : RouteUiState()
    data class RoutesCalculated(val routes: List<Route>) : RouteUiState()
    data class RoutesReady(val routes: List<Route>) : RouteUiState()
    data class Navigating(
        val route: Route,
        val instructions: List<NavigationInstruction> = emptyList(),
        val currentStep: Int = 0
    ) : RouteUiState()
    object Arrived : RouteUiState()
    object Completed : RouteUiState()
    data class Error(val message: String) : RouteUiState()
}

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val calculateRoutesUseCase: CalculateRoutesUseCase,
    private val safetyMonitorUseCase: SafetyMonitorUseCase,
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _state = MutableStateFlow<RouteUiState>(RouteUiState.Idle)
    val state: StateFlow<RouteUiState> = _state.asStateFlow()

    // Backward compatible alias
    private val _uiState get() = _state
    val uiState: StateFlow<RouteUiState> get() = state

    private val _availableRoutes = MutableStateFlow<List<Route>>(emptyList())
    val availableRoutes: StateFlow<List<Route>> = _availableRoutes.asStateFlow()

    val safetyState: StateFlow<SafetyState> = safetyMonitorUseCase.safetyState

    fun calculateRoutes(origin: GeoPoint, destination: GeoPoint) {
        _state.value = RouteUiState.Loading
        viewModelScope.launch {
            calculateRoutesUseCase(origin, destination)
                .onSuccess { routes ->
                    _availableRoutes.value = routes
                    _state.value = RouteUiState.RoutesReady(routes)
                }
                .onFailure { error ->
                    _state.value = RouteUiState.Error(error.message ?: "Error al calcular rutas")
                }
        }
    }

    fun findRoutes(origin: GeoPoint, destination: GeoPoint) {
        _state.value = RouteUiState.Loading
        viewModelScope.launch {
            calculateRoutesUseCase(origin, destination)
                .onSuccess { routes ->
                    _availableRoutes.value = routes
                    _state.value = RouteUiState.RoutesCalculated(routes)
                }
                .onFailure { error ->
                    _state.value = RouteUiState.Error(error.message ?: "Error al calcular rutas")
                }
        }
    }

    fun selectRoute(route: Route) {
        val instructions = routeRepository.getNavigationInstructions(route)
        _state.value = RouteUiState.Navigating(route, instructions, 0)
        viewModelScope.launch {
            routeRepository.setActiveRoute(route)
            safetyMonitorUseCase.startMonitoring(route, route.origin, viewModelScope)
        }
    }

    fun startNavigation(route: Route) {
        selectRoute(route)
    }

    fun advanceStep() {
        val current = _state.value
        if (current is RouteUiState.Navigating) {
            val nextStep = current.currentStep + 1
            if (nextStep >= current.instructions.size) {
                _state.value = RouteUiState.Arrived
            } else {
                _state.value = current.copy(currentStep = nextStep)
            }
        }
    }

    fun cancelNavigation() {
        viewModelScope.launch {
            routeRepository.clearActiveRoute()
            safetyMonitorUseCase.stopMonitoring()
            _state.value = RouteUiState.Idle
        }
    }

    fun recalculateActiveRoute() {
        val current = _state.value
        if (current is RouteUiState.Navigating) {
            viewModelScope.launch {
                routeRepository.recalculateRoute(current.route)
                    .onSuccess { updatedRoute ->
                        val instructions = routeRepository.getNavigationInstructions(updatedRoute)
                        _state.value = RouteUiState.Navigating(updatedRoute, instructions, current.currentStep)
                    }
                    .onFailure { error ->
                        _state.value = RouteUiState.Error(error.message ?: "Error al recalcular ruta")
                    }
            }
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
        _state.value = RouteUiState.Completed
    }
}
