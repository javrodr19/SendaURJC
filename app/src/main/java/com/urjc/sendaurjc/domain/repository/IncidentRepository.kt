package com.urjc.sendaurjc.domain.repository

import com.urjc.sendaurjc.domain.model.GeoPoint
import com.urjc.sendaurjc.domain.model.Incident
import com.urjc.sendaurjc.domain.model.IncidentStatus
import com.urjc.sendaurjc.domain.model.IncidentType
import com.urjc.sendaurjc.domain.model.Ticket
import kotlinx.coroutines.flow.Flow

interface IncidentRepository {
    // RF42: user reports incident
    suspend fun reportIncident(
        reporterId: String,
        type: IncidentType,
        description: String,
        location: GeoPoint
    ): Result<Incident>

    // RF44: auto ticket generation
    suspend fun generateTicket(incident: Incident): Result<Ticket>

    // RF43: admin panel – get all incidents
    fun observeAllIncidents(): Flow<List<Incident>>

    // RF43: update incident status
    suspend fun updateIncidentStatus(id: String, status: IncidentStatus): Result<Unit>

    suspend fun getIncident(id: String): Incident?
    suspend fun getTicket(incidentId: String): Ticket?
    suspend fun getAllTickets(): List<Ticket>
}
