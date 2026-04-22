package com.urjc.sendaurjc.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GeoPoint(
    val latitude: Double,
    val longitude: Double
) : Parcelable

@Parcelize
data class RouteSegment(
    val start: GeoPoint,
    val end: GeoPoint,
    val zoneId: String,
    val hasActiveLight: Boolean,      // RF33
    val trafficIndex: Int,            // RF34: 0-100
    val hasObstacles: Boolean,        // RF35
    val dataQuality: String
) : Parcelable

@Parcelize
data class Route(
    val id: String,
    val origin: GeoPoint,
    val destination: GeoPoint,
    val segments: List<RouteSegment>,
    val securityIndex: Double,         // RF31: 0-100
    val isSafest: Boolean = false,     // RF36
    val estimatedMinutes: Int,
    val scheduledDeparture: Long? = null,  // RF: programmed departure
    val companionRequested: Boolean = false
) : Parcelable

data class NavigationInstruction(
    val text: String,
    val distanceMeters: Double,
    val point: GeoPoint
)
