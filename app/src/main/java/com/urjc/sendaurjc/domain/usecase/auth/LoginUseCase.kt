package com.urjc.sendaurjc.domain.usecase.auth

import com.urjc.sendaurjc.domain.model.User
import com.urjc.sendaurjc.domain.repository.UserRepository
import javax.inject.Inject

/**
 * RF27, RF65: Login via SSO URJC and ensure unique profile
 */
class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, ssoToken: String): Result<User> {
        // RF65: Restricción de correos electrónicos institucionales
        if (!isValidUrjcEmail(email)) {
            return Result.failure(IllegalArgumentException("Only URJC institutional emails are allowed"))
        }

        // RF27: Acceso mediante autenticación centralizada
        val result = userRepository.loginWithSso(email, ssoToken)
        
        // RF28: Perfil único
        // Validation occurs inside repository / backend, but if successful, user is returned
        return result
    }

    private fun isValidUrjcEmail(email: String): Boolean {
        return email.endsWith("@urjc.es") || email.endsWith("@alumnos.urjc.es")
    }
}
