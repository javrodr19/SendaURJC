package com.urjc.sendaurjc.domain.repository

import com.urjc.sendaurjc.domain.model.CompanionRequest
import com.urjc.sendaurjc.domain.model.GeoPoint
import com.urjc.sendaurjc.domain.model.UserRating
import kotlinx.coroutines.flow.Flow

interface CompanionRepository {
    suspend fun requestCompanion(
        requesterId: String,
        requesterName: String,
        origin: GeoPoint,
        destination: GeoPoint,
        scheduledTime: Long
    ): Result<CompanionRequest>

    suspend fun acceptRequest(requestId: String, volunteerId: String): Result<CompanionRequest>
    suspend fun rejectRequest(requestId: String, volunteerId: String): Result<Unit>

    fun observePendingRequests(): Flow<List<CompanionRequest>>
    fun observeMyRequests(userId: String): Flow<List<CompanionRequest>>
    suspend fun getRequest(id: String): CompanionRequest?

    suspend fun saveUserRating(rating: UserRating): Result<Unit>
    suspend fun getAverageRating(userId: String, asVolunteer: Boolean): Double
}
