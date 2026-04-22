package com.urjc.sendaurjc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.urjc.sendaurjc.domain.model.User
import com.urjc.sendaurjc.domain.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val name: String,
    val surname: String,
    val role: String,
    val isVolunteer: Boolean,
    val trustedContactId: String?,
    val profileComplete: Boolean,
    val totalRoutes: Int,
    val totalCompanions: Int
) {
    fun toDomain() = User(
        id = id,
        email = email,
        name = name,
        surname = surname,
        role = UserRole.valueOf(role),
        isVolunteer = isVolunteer,
        trustedContactId = trustedContactId,
        profileComplete = profileComplete,
        totalRoutes = totalRoutes,
        totalCompanions = totalCompanions
    )

    companion object {
        fun fromDomain(u: User) = UserEntity(
            id = u.id,
            email = u.email,
            name = u.name,
            surname = u.surname,
            role = u.role.name,
            isVolunteer = u.isVolunteer,
            trustedContactId = u.trustedContactId,
            profileComplete = u.profileComplete,
            totalRoutes = u.totalRoutes,
            totalCompanions = u.totalCompanions
        )
    }
}

@Entity(tableName = "trusted_contacts")
data class TrustedContactEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val contactName: String,
    val contactPhone: String,
    val contactEmail: String
)
