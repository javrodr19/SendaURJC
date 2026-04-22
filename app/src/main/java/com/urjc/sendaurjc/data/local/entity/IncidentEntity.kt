package com.urjc.sendaurjc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.urjc.sendaurjc.domain.model.GeoPoint
import com.urjc.sendaurjc.domain.model.Incident
import com.urjc.sendaurjc.domain.model.IncidentStatus
import com.urjc.sendaurjc.domain.model.IncidentType
import com.urjc.sendaurjc.domain.model.Ticket

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey val id: String,
    val reporterId: String,
    val type: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val status: String,
    val ticketId: String?
) {
    fun toDomain() = Incident(
        id = id,
        reporterId = reporterId,
        type = IncidentType.valueOf(type),
        description = description,
        location = GeoPoint(latitude, longitude),
        timestamp = timestamp,
        status = IncidentStatus.valueOf(status),
        ticketId = ticketId
    )

    companion object {
        fun fromDomain(i: Incident) = IncidentEntity(
            id = i.id,
            reporterId = i.reporterId,
            type = i.type.name,
            description = i.description,
            latitude = i.location.latitude,
            longitude = i.location.longitude,
            timestamp = i.timestamp,
            status = i.status.name,
            ticketId = i.ticketId
        )
    }
}

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey val id: String,
    val incidentId: String,
    val createdAt: Long,
    val status: String,
    val resolvedAt: Long?
) {
    fun toDomain() = Ticket(
        id = id,
        incidentId = incidentId,
        createdAt = createdAt,
        status = IncidentStatus.valueOf(status),
        resolvedAt = resolvedAt
    )
}
