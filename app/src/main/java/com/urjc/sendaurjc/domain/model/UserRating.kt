package com.urjc.sendaurjc.domain.model

data class UserRating(
    val id: String,
    val fromUserId: String,
    val toUserId: String,
    val companionRequestId: String,
    val score: Int,
    val comment: String = "",
    val timestamp: Long,
    val asVolunteer: Boolean
)
