package com.urjc.sendaurjc.domain.model

data class CalibrationPreferences(
    val userId: String,
    val weightLighting: Float = 0.6f,
    val weightTraffic: Float = 0.25f,
    val weightEnvironment: Float = 0.15f,
    val preferSafest: Boolean = true,
    val avoidObstacles: Boolean = true
)
