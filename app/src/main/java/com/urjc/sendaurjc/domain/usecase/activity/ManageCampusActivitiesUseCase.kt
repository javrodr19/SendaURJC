package com.urjc.sendaurjc.domain.usecase.activity

import com.urjc.sendaurjc.domain.model.Activity
import com.urjc.sendaurjc.domain.model.Installation
import com.urjc.sendaurjc.domain.model.Team
import com.urjc.sendaurjc.domain.model.UserRole
import com.urjc.sendaurjc.domain.repository.CampusRepository
import com.urjc.sendaurjc.domain.repository.UserRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Implements CU07-CU14 related to activities, teams and installations.
 */
class ManageCampusActivitiesUseCase @Inject constructor(
    private val campusRepository: CampusRepository,
    private val userRepository: UserRepository
) {

    // CU14: Crear_Actividad (Only PAS)
    suspend fun createActivity(
        name: String,
        description: String,
        date: Long,
        totalSlots: Int,
        imageUrl: String? = null
    ): Result<Unit> {
        val user = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("Not authenticated"))
        
        if (user.role != UserRole.PTGAS && user.role != UserRole.ADMIN) {
            return Result.failure(Exception("Only staff can create activities"))
        }

        val activity = Activity(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            date = date,
            totalSlots = totalSlots,
            availableSlots = totalSlots,
            imageUrl = imageUrl
        )
        return campusRepository.createActivity(activity)
    }

    // CU07: Apuntarse_actividad
    suspend fun joinActivity(activityId: String): Result<Unit> {
        val user = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("Not authenticated"))
        
        val activity = campusRepository.getActivity(activityId)
            ?: return Result.failure(Exception("Activity not found"))
        
        if (activity.availableSlots <= 0) {
            return Result.failure(Exception("No slots available"))
        }

        return campusRepository.joinActivity(user.id, activityId)
    }

    // CU08: CrearEquipo
    suspend fun createTeam(name: String, activityId: String): Result<Unit> {
        val user = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("Not authenticated"))
        
        val team = Team(
            id = UUID.randomUUID().toString(),
            name = name,
            captainId = user.id,
            activityId = activityId,
            memberIds = listOf(user.id)
        )
        return campusRepository.createTeam(team)
    }

    // CU09: Unir equipo
    suspend fun joinTeam(teamId: String): Result<Unit> {
        val user = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("Not authenticated"))
        
        return campusRepository.joinTeam(user.id, teamId)
    }

    // CU13: Alquilar_Instalación
    suspend fun rentInstallation(installationId: String): Result<Unit> {
        val user = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("Not authenticated"))
        
        return campusRepository.rentInstallation(user.id, installationId)
    }

    suspend fun getInstallations(): List<Installation> {
        return campusRepository.getInstallations()
    }
}
