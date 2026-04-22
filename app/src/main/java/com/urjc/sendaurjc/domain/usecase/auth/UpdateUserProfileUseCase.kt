package com.urjc.sendaurjc.domain.usecase.auth

import com.urjc.sendaurjc.domain.model.User
import com.urjc.sendaurjc.domain.repository.UserRepository
import javax.inject.Inject

/**
 * RF66: Actualización del perfil de usuario
 * RF67: Gestión de la foto de perfil
 * RF68: Indicación del género en el perfil
 * RF69: Edición de información no fundamental del perfil
 * RF64: Indicador de información pendiente en el perfil
 */
class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    // RF66, RF67, RF68, RF69: Update different properties of user profile
    suspend fun updateProfile(
        name: String? = null,
        surname: String? = null,
        photoUrl: String? = null,
        gender: String? = null
    ): Result<User> {
        val currentUser = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("User not authenticated"))

        val updatedUser = currentUser.copy(
            name = name ?: currentUser.name,
            surname = surname ?: currentUser.surname,
            photoUrl = photoUrl ?: currentUser.photoUrl,
            gender = gender ?: currentUser.gender
        )

        // Evaluate if profile is fully complete (RF64 related)
        val finalUser = updatedUser.copy(
            profileComplete = calculateIfProfileComplete(updatedUser)
        )

        val result = userRepository.updateProfile(finalUser)
        return if (result.isSuccess) {
            Result.success(finalUser)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
        }
    }

    // RF64: Calculates what is missing in the profile
    fun getPendingProfileInfo(user: User): List<String> {
        val pending = mutableListOf<String>()
        
        if (user.photoUrl.isNullOrBlank()) {
            pending.add("Foto de perfil")
        }
        if (user.gender.isNullOrBlank()) {
            pending.add("Género")
        }
        if (user.trustedContactId.isNullOrBlank()) {
            pending.add("Contacto de confianza")
        }
        
        return pending
    }

    private fun calculateIfProfileComplete(user: User): Boolean {
        return getPendingProfileInfo(user).isEmpty()
    }
}
