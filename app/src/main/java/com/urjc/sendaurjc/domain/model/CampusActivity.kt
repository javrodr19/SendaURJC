package com.urjc.sendaurjc.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Activity(
    val id: String,
    val name: String,
    val description: String,
    val date: Long,
    val totalSlots: Int,
    val availableSlots: Int,
    val imageUrl: String? = null,
    val participantsIds: List<String> = emptyList()
) : Parcelable

@Parcelize
data class Team(
    val id: String,
    val name: String,
    val captainId: String,
    val activityId: String,
    val memberIds: List<String> = emptyList(),
    val shieldUrl: String? = null
) : Parcelable

@Parcelize
data class Installation(
    val id: String,
    val name: String,
    val description: String,
    val available: Boolean = true,
    val location: GeoPoint? = null
) : Parcelable
