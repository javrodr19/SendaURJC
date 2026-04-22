package com.urjc.sendaurjc.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.urjc.sendaurjc.data.local.dao.UserDao
import com.urjc.sendaurjc.data.local.entity.TrustedContactEntity
import com.urjc.sendaurjc.data.local.entity.UserEntity
import com.urjc.sendaurjc.data.remote.api.UrjcApi
import com.urjc.sendaurjc.data.remote.dto.SsoLoginRequestDto
import com.urjc.sendaurjc.domain.model.TrustedContact
import com.urjc.sendaurjc.domain.model.User
import com.urjc.sendaurjc.domain.model.UserRole
import com.urjc.sendaurjc.domain.repository.UserRepository
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val dao: UserDao,
    private val api: UrjcApi,
    private val dataStore: DataStore<Preferences>
) : UserRepository {

    private val TOKEN_KEY = stringPreferencesKey("auth_token")
    private val REFRESH_KEY = stringPreferencesKey("refresh_token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        val response = api.login(SsoLoginRequestDto(username = email, password = password))
        val body = response.body() ?: error("Login failed: ${response.code()}")
        val user = User(
            id = body.userId,
            email = body.email,
            name = body.name,
            surname = body.surname,
            role = UserRole.valueOf(body.role.uppercase()),
            isVolunteer = false,
            profileComplete = true
        )
        dao.upsert(UserEntity.fromDomain(user))
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = body.accessToken
            prefs[REFRESH_KEY] = body.refreshToken
            prefs[USER_ID_KEY] = user.id
        }
        user
    }

    override suspend fun loginWithSSO(token: String): Result<User> = runCatching {
        val response = api.getProfile("Bearer $token")
        val profile = response.body() ?: error("Failed to get profile: ${response.code()}")
        val user = User(
            id = profile.id,
            email = profile.email,
            name = profile.name,
            surname = profile.surname,
            role = UserRole.valueOf(profile.role.uppercase()),
            isVolunteer = profile.isVolunteer,
            profileComplete = profile.profileComplete,
            totalRoutes = profile.totalRoutes,
            totalCompanions = profile.totalCompanions
        )
        dao.upsert(UserEntity.fromDomain(user))
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = user.id
        }
        user
    }

    override suspend fun logout() {
        dataStore.edit { it.clear() }
    }

    override suspend fun getCurrentUser(): User? {
        val userId = dataStore.data.first()[USER_ID_KEY] ?: return null
        return dao.getById(userId)?.toDomain()
    }

    override fun observeCurrentUser(): Flow<User?> =
        dataStore.data.map { prefs -> prefs[USER_ID_KEY] }
            .flatMapLatest { userId ->
                if (userId != null) dao.observe(userId).map { it?.toDomain() }
                else kotlinx.coroutines.flow.flowOf(null)
            }

    override suspend fun updateProfile(user: User): Result<Unit> = runCatching {
        dao.upsert(UserEntity.fromDomain(user))
    }

    override suspend fun setTrustedContact(contact: TrustedContact): Result<Unit> = runCatching {
        dao.upsertTrustedContact(
            TrustedContactEntity(
                id = contact.id,
                userId = contact.userId,
                contactName = contact.contactName,
                contactPhone = contact.contactPhone,
                contactEmail = contact.contactEmail
            )
        )
    }

    override suspend fun getTrustedContact(userId: String): TrustedContact? {
        val entity = dao.getTrustedContact(userId) ?: return null
        return TrustedContact(
            id = entity.id,
            userId = entity.userId,
            contactName = entity.contactName,
            contactPhone = entity.contactPhone,
            contactEmail = entity.contactEmail
        )
    }

    override suspend fun isLoggedIn(): Boolean =
        dataStore.data.first()[TOKEN_KEY] != null

    override suspend fun getStoredToken(): String? =
        dataStore.data.first()[TOKEN_KEY]

    override suspend fun refreshToken(): Result<String> = runCatching {
        val refreshToken = dataStore.data.first()[REFRESH_KEY] ?: error("No refresh token")
        val response = api.refreshToken("Bearer $refreshToken")
        val body = response.body() ?: error("Refresh failed: ${response.code()}")
        dataStore.edit { prefs -> prefs[TOKEN_KEY] = body.accessToken }
        body.accessToken
    }

    // In-memory store for calibration preferences (production would use a DAO/table)
    private val calibrationStore = mutableMapOf<String, com.urjc.sendaurjc.domain.model.CalibrationPreferences>()

    override suspend fun getCalibrationPreferences(userId: String): com.urjc.sendaurjc.domain.model.CalibrationPreferences {
        return calibrationStore[userId] ?: com.urjc.sendaurjc.domain.model.CalibrationPreferences(userId)
    }

    override suspend fun saveCalibrationPreferences(preferences: com.urjc.sendaurjc.domain.model.CalibrationPreferences): Result<Unit> = runCatching {
        calibrationStore[preferences.userId] = preferences
    }
}
