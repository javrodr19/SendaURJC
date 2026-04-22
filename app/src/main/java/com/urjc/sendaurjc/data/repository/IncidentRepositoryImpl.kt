package com.urjc.sendaurjc.data.repository

import com.urjc.sendaurjc.data.local.dao.IncidentDao
import com.urjc.sendaurjc.data.local.entity.IncidentEntity
import com.urjc.sendaurjc.data.local.entity.TicketEntity
import com.urjc.sendaurjc.domain.model.*
import com.urjc.sendaurjc.domain.repository.IncidentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncidentRepositoryImpl @Inject constructor(
    private val dao: IncidentDao
) : IncidentRepository {

    override suspend fun reportIncident(
        reporterId: String,
        type: IncidentType,
        description: String,
        location: GeoPoint
    ): Result<Incident> = runCatching {
        require(description.isNotBlank()) { "Description cannot be empty" }
        val incident = Incident(
            id = UUID.randomUUID().toString(),
            reporterId = reporterId,
            type = type,
            description = description,
            location = location,
            timestamp = System.currentTimeMillis(),
            status = IncidentStatus.ABIERTA
        )
        dao.insert(IncidentEntity.fromDomain(incident))
        incident
    }

    // RF44: auto-generate ticket for every incident
    override suspend fun generateTicket(incident: Incident): Result<Ticket> = runCatching {
        val ticket = Ticket(
            id = UUID.randomUUID().toString(),
            incidentId = incident.id,
            createdAt = System.currentTimeMillis(),
            status = IncidentStatus.ABIERTA
        )
        dao.insertTicket(
            TicketEntity(
                id = ticket.id,
                incidentId = ticket.incidentId,
                createdAt = ticket.createdAt,
                status = ticket.status.name,
                resolvedAt = null
            )
        )
        dao.linkTicket(incident.id, ticket.id)
        ticket
    }

    override fun observeAllIncidents(): Flow<List<Incident>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun updateIncidentStatus(id: String, status: IncidentStatus): Result<Unit> =
        runCatching { dao.updateStatus(id, status.name) }

    override suspend fun getIncident(id: String): Incident? =
        dao.getById(id)?.toDomain()

    override suspend fun getTicket(incidentId: String): Ticket? =
        dao.getTicketByIncident(incidentId)?.toDomain()

    override suspend fun getAllTickets(): List<Ticket> =
        dao.getAllTickets().map { it.toDomain() }
}
