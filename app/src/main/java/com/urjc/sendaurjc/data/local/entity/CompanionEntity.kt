package com.urjc.sendaurjc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.urjc.sendaurjc.domain.model.CompanionRequest
import com.urjc.sendaurjc.domain.model.CompanionStatus
import com.urjc.sendaurjc.domain.model.GeoPoint

@Entity(tableName = "companion_requests")
data class CompanionRequestEntity(
    @PrimaryKey val id: String,
    val requesterId: String,
    val requesterName: String,
    val volunteerId: String?,
    val originLat: Double,
    val originLng: Double,
    val destinationLat: Double,
    val destinationLng: Double,
    val scheduledTime: Long,
    val status: String,
    val routeId: String?
) {
    fun toDomain() = CompanionRequest(
        id = id,
        requesterId = requesterId,
        requesterName = requesterName,
        volunteerId = volunteerId,
        origin = GeoPoint(originLat, originLng),
        destination = GeoPoint(destinationLat, destinationLng),
        scheduledTime = scheduledTime,
        status = CompanionStatus.valueOf(status),
        routeId = routeId
    )

    companion object {
        fun fromDomain(r: CompanionRequest) = CompanionRequestEntity(
            id = r.id,
            requesterId = r.requesterId,
            requesterName = r.requesterName,
            volunteerId = r.volunteerId,
            originLat = r.origin.latitude,
            originLng = r.origin.longitude,
            destinationLat = r.destination.latitude,
            destinationLng = r.destination.longitude,
            scheduledTime = r.scheduledTime,
            status = r.status.name,
            routeId = r.routeId
        )
    }
}
