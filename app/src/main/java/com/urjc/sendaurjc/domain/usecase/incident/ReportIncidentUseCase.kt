package com.urjc.sendaurjc.domain.usecase.incident

import com.urjc.sendaurjc.domain.model.GeoPoint
import com.urjc.sendaurjc.domain.model.Incident
import com.urjc.sendaurjc.domain.model.IncidentType
import com.urjc.sendaurjc.domain.repository.IncidentRepository
import com.urjc.sendaurjc.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Implements Incident reporting from users (RF42, RF104, RF105, RF106, RF107)
 * and automatic ticket generation (RF44).
 */
class ReportIncidentUseCase @Inject constructor(
    private val incidentRepository: IncidentRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        type: IncidentType,
        description: String,
        location: GeoPoint
    ): Result<Incident> {
        val userId = userRepository.getCurrentUser()?.id
            ?: return Result.failure(Exception("User must be logged in to report incidents"))

        // RF42, RF104: Report general incident
        // RF105: Reportar farolas fundidas (type == FAROLA_FUNDIDA or similar)
        // RF106: Reportar zonas solitarias
        // RF107: Reportar obstáculos en la vía
        val reportResult = incidentRepository.reportIncident(
            reporterId = userId,
            type = type,
            description = description,
            location = location
        )

        if (reportResult.isSuccess) {
            val incident = reportResult.getOrThrow()
            
            // RF44: Generación automática de tickets
            val ticketResult = incidentRepository.generateTicket(incident)
            
            if (ticketResult.isFailure) {
                // If ticket generation fails, we might want to log it or handle it, 
                // but the incident is already reported. We'll return the incident anyway
                // or you could choose to return failure.
                return Result.failure(Exception("Incident reported but ticket generation failed: ${ticketResult.exceptionOrNull()?.message}"))
            }
        }

        return reportResult
    }
}
