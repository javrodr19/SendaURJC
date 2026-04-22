package com.urjc.sendaurjc.domain.model

data class RouteHistory(
    val id: String,
    val userId: String,
    val origin: GeoPoint,
    val destination: GeoPoint,
    val startedAt: Long,
    val completedAt: Long,
    val distanceMeters: Double,
    val securityIndex: Double,
    val hadCompanion: Boolean,
    val routeRating: Int? = null
)
