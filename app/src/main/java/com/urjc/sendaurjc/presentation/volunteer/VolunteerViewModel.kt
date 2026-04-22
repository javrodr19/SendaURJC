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

@HiltViewModel
class VolunteerViewModel @Inject constructor(
    private val manageCompanionUseCase: ManageCompanionUseCase
) : ViewModel() {

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
            }
        }
    }

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            manageCompanionUseCase.acceptRequest(requestId)
                .onSuccess {
                    _uiState.value = VolunteerUiState.RequestActionSuccess("Solicitud aceptada")
                }
                .onFailure {
                    _uiState.value = VolunteerUiState.Error("No se pudo aceptar la solicitud")
                }
        }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            manageCompanionUseCase.rejectRequest(requestId)
                .onSuccess {
                    _uiState.value = VolunteerUiState.RequestActionSuccess("Solicitud rechazada")
                }
                .onFailure {
                    _uiState.value = VolunteerUiState.Error("No se pudo rechazar la solicitud")
                }
        }
    }

    sealed class VolunteerUiState {
        object Idle : VolunteerUiState()
        data class RequestActionSuccess(val message: String) : VolunteerUiState()
        data class Error(val message: String) : VolunteerUiState()
    }
}
