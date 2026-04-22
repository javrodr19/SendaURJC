package com.urjc.sendaurjc.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class CompanionStatus { PENDING, ACCEPTED, REJECTED, COMPLETED }

@Parcelize
data class CompanionRequest(
    val id: String,
    val requesterId: String,
    val requesterName: String,
    val volunteerId: String?,
    val origin: GeoPoint,
    val destination: GeoPoint,
    val scheduledTime: Long,
    val status: CompanionStatus = CompanionStatus.PENDING,
    val routeId: String? = null
) : Parcelable

data class TrustedContact(
    val id: String,
    val userId: String,
    val contactName: String,
    val contactPhone: String,
    val contactEmail: String
)
