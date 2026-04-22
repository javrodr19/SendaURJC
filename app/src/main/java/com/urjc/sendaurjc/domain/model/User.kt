package com.urjc.sendaurjc.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class UserRole { STUDENT, PDI, PTGAS, VOLUNTEER, ADMIN }

@Parcelize
data class User(
    val id: String,
    val email: String,
    val name: String,
    val surname: String,
    val role: UserRole,
    val isVolunteer: Boolean = false,
    val trustedContactId: String? = null,
    val profileComplete: Boolean = false,
    val totalRoutes: Int = 0,
    val totalCompanions: Int = 0,
    val photoUrl: String? = null,   // RF67
    val gender: String? = null      // RF68
) : Parcelable
