package com.urjc.sendaurjc.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urjc.sendaurjc.domain.model.CriticalAlert
import com.urjc.sendaurjc.domain.model.Incident
import com.urjc.sendaurjc.domain.model.IncidentStatus
import com.urjc.sendaurjc.domain.model.Ticket
import com.urjc.sendaurjc.domain.repository.AlertRepository
import com.urjc.sendaurjc.domain.repository.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val incidents: List<Incident> = emptyList(),
    val tickets: List<Ticket> = emptyList(),
    val unprocessedAlerts: List<CriticalAlert> = emptyList(),
    val message: String? = null
)

// RF43: admin panel for incident management
@HiltViewModel
class AdminViewModel @Inject constructor(
    private val incidentRepository: IncidentRepository,
    private val alertRepository: AlertRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUiState())
    val state: StateFlow<AdminUiState> = _state

    init {
        observeIncidents()
        observeAlerts()
        loadTickets()
    }

    private fun observeIncidents() {
        viewModelScope.launch {
            incidentRepository.observeAllIncidents().collect { list ->
                _state.value = _state.value.copy(incidents = list)
            }
        }
    }

    private fun observeAlerts() {
        viewModelScope.launch {
            alertRepository.observeUnprocessedAlerts().collect { list ->
                _state.value = _state.value.copy(unprocessedAlerts = list)
            }
        }
    }

    private fun loadTickets() {
        viewModelScope.launch {
            val tickets = incidentRepository.getAllTickets()
            _state.value = _state.value.copy(tickets = tickets)
        }
    }

    fun updateStatus(incidentId: String, status: IncidentStatus) {
        viewModelScope.launch {
            incidentRepository.updateIncidentStatus(incidentId, status)
                .onSuccess {
                    _state.value = _state.value.copy(
                        message = "Estado actualizado a: ${status.name}"
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(message = "Error: ${e.message}")
                }
        }
    }

    // RF18: process critical alert within 10s
    fun processAlert(alert: CriticalAlert) {
        viewModelScope.launch {
            alertRepository.processCriticalAlert(alert)
                .onSuccess { _state.value = _state.value.copy(message = "Alerta procesada") }
                .onFailure { e -> _state.value = _state.value.copy(message = "Error: ${e.message}") }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
