package com.urjc.sendaurjc.domain.usecase.auth

import com.urjc.sendaurjc.domain.model.User
import com.urjc.sendaurjc.domain.model.UserRole
import com.urjc.sendaurjc.domain.repository.UserRepository
import java.util.UUID
import javax.inject.Inject

/**
 * CU01 - RegistrarUsuarioURJC
 * CU02 - RegistrarUsuarioExterno
 * CU03 - ConfirmarAlta
 */
class RegisterUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    // CU01: RegistrarUsuarioURJC
    suspend fun registerUrjcUser(
        fullName: String,
        dni: String,
        birthDate: Long,
        phone: String,
        email: String,
        role: UserRole
    ): Result<Unit> {
        if (!email.endsWith("@urjc.es") && !email.endsWith("@alumnos.urjc.es")) {
            return Result.failure(IllegalArgumentException("Email must be institutional"))
        }
        
        // Simulating the registration process as per CU01 steps
        // 1. User enters data
        // 2. LDAP verification (handled in repository/backend)
        // 3. Confirmation email (handled in repository/backend)
        
        val newUser = User(
            id = UUID.randomUUID().toString(),
            email = email,
            name = fullName.split(" ").firstOrNull() ?: fullName,
            surname = fullName.split(" ").getOrNull(1) ?: "",
            role = role,
            profileComplete = false
        )

        return userRepository.updateProfile(newUser)
    }

    // CU02: RegistrarUsuarioExterno
    suspend fun registerExternalUser(
        fullName: String,
        dni: String,
        birthDate: Long,
        phone: String,
        email: String
    ): Result<Unit> {
        // CU02 explicitly allows external users.
        // Note: This contradicts RF65, but as per instructions I should follow all specified in PDF.
        // RF65 says "uniquely via URJC emails". CU02 says "non-URJC linked users".
        // I will implement it and if backend repository enforces RF65 it will fail there.
        
        val newUser = User(
            id = UUID.randomUUID().toString(),
            email = email,
            name = fullName.split(" ").firstOrNull() ?: fullName,
            surname = fullName.split(" ").getOrNull(1) ?: "",
            role = UserRole.STUDENT, // Default external role
            profileComplete = false
        )

        return userRepository.updateProfile(newUser)
    }
}
