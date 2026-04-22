package com.urjc.sendaurjc.domain.usecase.companion

import com.urjc.sendaurjc.domain.model.CompanionStatus
import com.urjc.sendaurjc.domain.model.UserRating
import com.urjc.sendaurjc.domain.repository.CompanionRepository
import com.urjc.sendaurjc.domain.repository.UserRepository
import java.util.UUID
import javax.inject.Inject

/**
 * RF76: Sistema de puntuación del acompañado
 * RF77: Sistema de puntuación del voluntario
 * RF91: Puntuación del acompañado
 * RF92: Puntuación del voluntario
 */
class RateUserUseCase @Inject constructor(
    private val companionRepository: CompanionRepository,
    private val userRepository: UserRepository
) {
    // RF91, RF92: Puntuar al finalizar el acompañamiento
    suspend fun rateUser(
        requestId: String,
        score: Int,
        comment: String = ""
    ): Result<Unit> {
        require(score in 1..5) { "Score must be between 1 and 5" }

        val currentUser = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("User not authenticated"))

        val request = companionRepository.getRequest(requestId)
            ?: return Result.failure(Exception("Companion request not found"))

        // RF93: Only allow rating if the route is completed
        if (request.status != CompanionStatus.COMPLETED) {
            return Result.failure(Exception("Cannot rate an incomplete request"))
        }

        val isVolunteerRatingRequester = currentUser.id == request.volunteerId
        val targetUserId = if (isVolunteerRatingRequester) request.requesterId else request.volunteerId

        if (targetUserId == null) {
            return Result.failure(Exception("Target user not found"))
        }

        val rating = UserRating(
            id = UUID.randomUUID().toString(),
            fromUserId = currentUser.id,
            toUserId = targetUserId,
            companionRequestId = requestId,
            score = score,
            comment = comment,
            timestamp = System.currentTimeMillis(),
            asVolunteer = !isVolunteerRatingRequester // If I am volunteer, the target is the requester (not a volunteer)
        )

        return companionRepository.saveUserRating(rating)
    }

    // RF76, RF77: Obtener la puntuación media
    suspend fun getAverageRating(userId: String, asVolunteer: Boolean): Double {
        return companionRepository.getAverageRating(userId, asVolunteer)
    }
}
