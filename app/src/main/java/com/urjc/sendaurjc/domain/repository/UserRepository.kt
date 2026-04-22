package com.urjc.sendaurjc.domain.repository

import com.urjc.sendaurjc.domain.model.CalibrationPreferences
import com.urjc.sendaurjc.domain.model.TrustedContact
import com.urjc.sendaurjc.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun loginWithSSO(token: String): Result<User>          // RF27
    suspend fun logout()
    suspend fun getCurrentUser(): User?
    fun observeCurrentUser(): Flow<User?>
    suspend fun updateProfile(user: User): Result<Unit>            // RF28
    suspend fun setTrustedContact(contact: TrustedContact): Result<Unit>  // RF40
    suspend fun getTrustedContact(userId: String): TrustedContact?
    suspend fun isLoggedIn(): Boolean
    suspend fun getStoredToken(): String?
    suspend fun refreshToken(): Result<String>                     // RF16: OAuth token refresh
    
    suspend fun getCalibrationPreferences(userId: String): CalibrationPreferences
    suspend fun saveCalibrationPreferences(preferences: CalibrationPreferences): Result<Unit>
}
