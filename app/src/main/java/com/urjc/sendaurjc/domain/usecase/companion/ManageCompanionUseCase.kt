package com.urjc.sendaurjc.domain.usecase.companion

import com.urjc.sendaurjc.domain.model.CompanionRequest
import com.urjc.sendaurjc.domain.model.GeoPoint
import com.urjc.sendaurjc.domain.repository.CompanionRepository
import com.urjc.sendaurjc.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * RF82, RF53, RF79, RF89: Volunteer and Companion operations.
 */
class ManageCompanionUseCase @Inject constructor(
    private val companionRepository: CompanionRepository,
    private val userRepository: UserRepository
) {
    // RF82: Solicitud de acompañamiento
    suspend fun requestCompanion(
        origin: GeoPoint,
        destination: GeoPoint,
        scheduledTime: Long
    ): Result<CompanionRequest> {
        val user = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("User not authenticated"))

        return companionRepository.requestCompanion(
            requesterId = user.id,
            requesterName = user.name,
            origin = origin,
            destination = destination,
            scheduledTime = scheduledTime
        )
    }

    // RF79: Aceptar solicitud
    suspend fun acceptRequest(requestId: String): Result<CompanionRequest> {
        val userId = userRepository.getCurrentUser()?.id
            ?: return Result.failure(Exception("User not authenticated"))

        return companionRepository.acceptRequest(requestId, userId)
    }

    // RF79: Rechazar solicitud
    suspend fun rejectRequest(requestId: String): Result<Unit> {
        val userId = userRepository.getCurrentUser()?.id
            ?: return Result.failure(Exception("User not authenticated"))

        return companionRepository.rejectRequest(requestId, userId)
    }

    // RF89: Cancelar un acompañamiento aceptado previamente
    suspend fun cancelAcceptedCompanion(requestId: String): Result<Unit> {
        val userId = userRepository.getCurrentUser()?.id
            ?: return Result.failure(Exception("User not authenticated"))
        
        // En un caso real, el backend notificaría al solicitante.
        // Aquí cancelamos a nivel repositorio.
        val result = companionRepository.rejectRequest(requestId, userId)

        // RF53: Al cancelar, el solicitante puede pedir nuevo voluntario.
        // Esta lógica normalmente residiría en un servicio de notificaciones
        // push hacia el usuario solicitante para que lance 'requestCompanion' de nuevo.

        return result
    }

    // RF78: Visualizar solicitudes disponibles
    fun observePendingRequests(): Flow<List<CompanionRequest>> {
        return companionRepository.observePendingRequests()
    }
}
