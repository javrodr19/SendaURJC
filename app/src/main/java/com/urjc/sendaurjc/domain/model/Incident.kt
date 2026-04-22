package com.urjc.sendaurjc.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class IncidentType { LUZ, AFLUENCIA, OBSTACULO, OTRO }
enum class IncidentStatus { ABIERTA, EN_GESTION, RESUELTA }

@Parcelize
data class Incident(
    val id: String,
    val reporterId: String,
    val type: IncidentType,
    val description: String,
    val location: GeoPoint,
    val timestamp: Long,
    val status: IncidentStatus = IncidentStatus.ABIERTA,
    val ticketId: String? = null        // RF44: auto-generated ticket
) : Parcelable

data class Ticket(
    val id: String,
    val incidentId: String,
    val createdAt: Long,
    val status: IncidentStatus,
    val resolvedAt: Long? = null
)
