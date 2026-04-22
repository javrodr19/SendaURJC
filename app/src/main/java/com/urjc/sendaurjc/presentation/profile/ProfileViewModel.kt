package com.urjc.sendaurjc.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urjc.sendaurjc.domain.model.RouteHistory
import com.urjc.sendaurjc.domain.model.TrustedContact
import com.urjc.sendaurjc.domain.model.User
import com.urjc.sendaurjc.domain.repository.UserRepository
import com.urjc.sendaurjc.domain.usecase.auth.UpdateUserProfileUseCase
import com.urjc.sendaurjc.domain.usecase.route.RouteHistoryUseCase
import com.urjc.sendaurjc.domain.usecase.route.UserStatistics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ProfileState(
    val user: User? = null,
    val isEditing: Boolean = false,
    val trustedContact: TrustedContact? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
    val routeHistory: List<RouteHistory> = emptyList(),
    val statistics: UserStatistics? = null,
    val pendingInfo: List<String> = emptyList()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val routeHistoryUseCase: RouteHistoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    // Keep backward-compatible properties
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _routeHistory = MutableStateFlow<List<RouteHistory>>(emptyList())
    val routeHistory: StateFlow<List<RouteHistory>> = _routeHistory.asStateFlow()

    private val _statistics = MutableStateFlow<UserStatistics?>(null)
    val statistics: StateFlow<UserStatistics?> = _statistics.asStateFlow()

    private val _pendingInfo = MutableStateFlow<List<String>>(emptyList())
    val pendingInfo: StateFlow<List<String>> = _pendingInfo.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            userRepository.observeCurrentUser().collect { user ->
                _currentUser.value = user
                _state.value = _state.value.copy(user = user)
                user?.let {
                    val pending = updateUserProfileUseCase.getPendingProfileInfo(it)
                    _pendingInfo.value = pending
                    _state.value = _state.value.copy(pendingInfo = pending)

                    // Load trusted contact
                    val contact = userRepository.getTrustedContact(it.id)
                    _state.value = _state.value.copy(trustedContact = contact)

                    loadHistoryAndStats()
                }
            }
        }
    }

    private suspend fun loadHistoryAndStats() {
        routeHistoryUseCase.getUserRouteHistory().onSuccess { history ->
            _routeHistory.value = history
            _state.value = _state.value.copy(routeHistory = history)
        }
        routeHistoryUseCase.getUserStatistics().onSuccess { stats ->
            _statistics.value = stats
            _state.value = _state.value.copy(statistics = stats)
        }
    }

    fun setEditing(editing: Boolean) {
        _state.value = _state.value.copy(isEditing = editing)
    }

    fun updateProfile(name: String, surname: String, isVolunteer: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            updateUserProfileUseCase.updateProfile(name = name, surname = surname)
                .onSuccess { updatedUser ->
                    // Also update isVolunteer if needed
                    val finalUser = updatedUser.copy(isVolunteer = isVolunteer)
                    userRepository.updateProfile(finalUser)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isEditing = false,
                        message = "Perfil actualizado"
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        message = "Error: ${e.message}"
                    )
                }
        }
    }

    // Overloaded for use cases that pass all 4 params
    fun updateProfile(name: String?, surname: String?, photoUrl: String?, gender: String?) {
        viewModelScope.launch {
            updateUserProfileUseCase.updateProfile(name, surname, photoUrl, gender)
        }
    }

    fun setTrustedContact(name: String, phone: String, email: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val userId = _state.value.user?.id ?: return@launch
            val contact = TrustedContact(
                id = UUID.randomUUID().toString(),
                userId = userId,
                contactName = name,
                contactPhone = phone,
                contactEmail = email
            )
            userRepository.setTrustedContact(contact)
                .onSuccess {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        trustedContact = contact,
                        message = "Contacto de confianza guardado"
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        message = "Error: ${e.message}"
                    )
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
