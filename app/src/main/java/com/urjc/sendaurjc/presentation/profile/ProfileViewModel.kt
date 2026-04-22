package com.urjc.sendaurjc.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urjc.sendaurjc.domain.model.RouteHistory
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
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val routeHistoryUseCase: RouteHistoryUseCase
) : ViewModel() {

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
                user?.let {
                    _pendingInfo.value = updateUserProfileUseCase.getPendingProfileInfo(it)
                    loadHistoryAndStats()
                }
            }
        }
    }

    private suspend fun loadHistoryAndStats() {
        routeHistoryUseCase.getUserRouteHistory().onSuccess { history ->
            _routeHistory.value = history
        }
        routeHistoryUseCase.getUserStatistics().onSuccess { stats ->
            _statistics.value = stats
        }
    }

    fun updateProfile(name: String?, surname: String?, photoUrl: String?, gender: String?) {
        viewModelScope.launch {
            updateUserProfileUseCase.updateProfile(name, surname, photoUrl, gender)
        }
    }
}
