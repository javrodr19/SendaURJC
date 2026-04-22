package com.urjc.sendaurjc.presentation.incidents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urjc.sendaurjc.domain.model.GeoPoint
import com.urjc.sendaurjc.domain.model.Incident
import com.urjc.sendaurjc.domain.model.IncidentType
import com.urjc.sendaurjc.domain.repository.IncidentRepository
import com.urjc.sendaurjc.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IncidentUiState(
    val incidents: List<Incident> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class IncidentViewModel @Inject constructor(
    private val incidentRepository: IncidentRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(IncidentUiState())
    val state: StateFlow<IncidentUiState> = _state

    init {
        viewModelScope.launch {
            incidentRepository.observeAllIncidents().collect { list ->
                _state.value = _state.value.copy(incidents = list)
            }
        }
    }

    // RF42: report incident
    fun reportIncident(type: IncidentType, description: String, location: GeoPoint) {
        if (description.isBlank()) {
            _state.value = _state.value.copy(message = "La descripción no puede estar vacía")
            return
        }
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch
            _state.value = _state.value.copy(isLoading = true)
            incidentRepository.reportIncident(user.id, type, description, location)
                .onSuccess { incident ->
                    // RF44: auto-generate ticket
                    incidentRepository.generateTicket(incident)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        message = "Incidencia registrada. Ticket generado automáticamente."
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        message = "Error al reportar: ${e.message}"
                    )
                }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
