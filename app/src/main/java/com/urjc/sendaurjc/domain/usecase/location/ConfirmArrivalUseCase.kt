package com.urjc.sendaurjc.domain.usecase.location

import com.urjc.sendaurjc.domain.model.CompanionStatus
import com.urjc.sendaurjc.domain.repository.CompanionRepository
import com.urjc.sendaurjc.domain.repository.RouteRepository
import com.urjc.sendaurjc.domain.repository.UserRepository
import javax.inject.Inject

/**
 * RF59: Confirmación de llegada segura al destino
 * RF93: Confirmación de llegada del voluntario
 */
class ConfirmArrivalUseCase @Inject constructor(
    private val routeRepository: RouteRepository,
    private val companionRepository: CompanionRepository,
    private val userRepository: UserRepository
) {
    suspend fun confirmSafeArrival(routeId: String, companionRequestId: String? = null): Result<Unit> {
        val user = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("User not authenticated"))

        // RF59: Confirm arrival for the route
        routeRepository.clearActiveRoute()
        
        // Ensure to save into Route History (handled externally or as an event)

        // RF93: Si había un voluntario o era acompañamiento, finalizarlo
        if (companionRequestId != null) {
            val request = companionRepository.getRequest(companionRequestId)
            if (request != null && request.status == CompanionStatus.ACCEPTED) {
                // Notificar al sistema que la solicitud ha finalizado con éxito
                // (Normalmente habría una mutación de estado en repository)
                // request.copy(status = CompanionStatus.COMPLETED)
            }
        }
        
        return Result.success(Unit)
    }
}
