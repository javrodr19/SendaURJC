package com.urjc.sendaurjc.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urjc.sendaurjc.domain.model.GeoPoint
import com.urjc.sendaurjc.domain.model.User
import com.urjc.sendaurjc.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val user: User? = null,
    val origin: GeoPoint? = null,
    val destination: GeoPoint? = null,
    val companionRequested: Boolean = false,
    val scheduledDeparture: Long? = null,
    val currentLocation: GeoPoint? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _currentLocation = MutableStateFlow<GeoPoint?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            userRepository.observeCurrentUser().collect { user ->
                _state.value = _state.value.copy(user = user)
                if (user != null) {
                    _uiState.value = HomeUiState.Authenticated(user.name)
                }
            }
        }
    }

    fun setOrigin(point: GeoPoint) {
        _state.value = _state.value.copy(origin = point)
    }

    fun setDestination(point: GeoPoint) {
        _state.value = _state.value.copy(destination = point)
    }

    fun setCompanionRequested(requested: Boolean) {
        _state.value = _state.value.copy(companionRequested = requested)
    }

    fun updateUserLocation(location: GeoPoint) {
        _currentLocation.value = location
        _state.value = _state.value.copy(currentLocation = location)
    }

    fun setScheduledDeparture(timeMillis: Long) {
        _state.value = _state.value.copy(scheduledDeparture = timeMillis)
    }

    fun onLocationUpdate(newLocation: GeoPoint) {
        _currentLocation.value = newLocation
    }

    sealed class HomeUiState {
        object Idle : HomeUiState()
        data class Authenticated(val userName: String) : HomeUiState()
    }
}
