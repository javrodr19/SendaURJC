package com.urjc.sendaurjc.presentation.volunteer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urjc.sendaurjc.domain.model.CompanionRequest
import com.urjc.sendaurjc.domain.usecase.companion.ManageCompanionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VolunteerState(
    val pendingRequests: List<CompanionRequest> = emptyList(),
    val message: String? = null
)

@HiltViewModel
class VolunteerViewModel @Inject constructor(
    private val manageCompanionUseCase: ManageCompanionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(VolunteerState())
    val state: StateFlow<VolunteerState> = _state.asStateFlow()

    private val _pendingRequests = MutableStateFlow<List<CompanionRequest>>(emptyList())
    val pendingRequests: StateFlow<List<CompanionRequest>> = _pendingRequests.asStateFlow()

    private val _uiState = MutableStateFlow<VolunteerUiState>(VolunteerUiState.Idle)
    val uiState: StateFlow<VolunteerUiState> = _uiState.asStateFlow()

    init {
        observeRequests()
    }

    private fun observeRequests() {
        viewModelScope.launch {
            manageCompanionUseCase.observePendingRequests().collect { requests ->
                _pendingRequests.value = requests
                _state.value = _state.value.copy(pendingRequests = requests)
            }
        }
    }

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            manageCompanionUseCase.acceptRequest(requestId)
                .onSuccess {
                    _uiState.value = VolunteerUiState.RequestActionSuccess("Solicitud aceptada")
                    _state.value = _state.value.copy(message = "Solicitud aceptada")
                }
                .onFailure {
                    _uiState.value = VolunteerUiState.Error("No se pudo aceptar la solicitud")
                    _state.value = _state.value.copy(message = "No se pudo aceptar la solicitud")
                }
        }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            manageCompanionUseCase.rejectRequest(requestId)
                .onSuccess {
                    _uiState.value = VolunteerUiState.RequestActionSuccess("Solicitud rechazada")
                    _state.value = _state.value.copy(message = "Solicitud rechazada")
                }
                .onFailure {
                    _uiState.value = VolunteerUiState.Error("No se pudo rechazar la solicitud")
                    _state.value = _state.value.copy(message = "No se pudo rechazar la solicitud")
                }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    sealed class VolunteerUiState {
        object Idle : VolunteerUiState()
        data class RequestActionSuccess(val message: String) : VolunteerUiState()
        data class Error(val message: String) : VolunteerUiState()
    }
}
