package com.urjc.sendaurjc.data.repository

import com.urjc.sendaurjc.domain.model.Activity
import com.urjc.sendaurjc.domain.model.Installation
import com.urjc.sendaurjc.domain.model.Team
import com.urjc.sendaurjc.domain.repository.CampusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CampusRepositoryImpl @Inject constructor() : CampusRepository {

    private val _activities = MutableStateFlow<Map<String, Activity>>(emptyMap())
    private val _teams = MutableStateFlow<Map<String, Team>>(emptyMap())
    private val _installations = MutableStateFlow<List<Installation>>(
        listOf(
            Installation("1", "Polideportivo", "Instalación deportiva principal"),
            Installation("2", "Aula Magna", "Aula de conferencias"),
            Installation("3", "Laboratorio L1", "Laboratorio de informática")
        )
    )

    override suspend fun createActivity(activity: Activity): Result<Unit> {
        _activities.value = _activities.value + (activity.id to activity)
        return Result.success(Unit)
    }

    override suspend fun getActivity(id: String): Activity? {
        return _activities.value[id]
    }

    override fun getAllActivities(): Flow<List<Activity>> {
        return _activities.asStateFlow().map { it.values.toList() }
    }

    override suspend fun joinActivity(userId: String, activityId: String): Result<Unit> {
        val activity = _activities.value[activityId] ?: return Result.failure(Exception("Not found"))
        if (activity.availableSlots <= 0) return Result.failure(Exception("No slots"))
        
        val updated = activity.copy(
            availableSlots = activity.availableSlots - 1,
            participantsIds = activity.participantsIds + userId
        )
        _activities.value = _activities.value + (activityId to updated)
        return Result.success(Unit)
    }

    override suspend fun createTeam(team: Team): Result<Unit> {
        _teams.value = _teams.value + (team.id to team)
        return Result.success(Unit)
    }

    override suspend fun getTeam(id: String): Team? {
        return _teams.value[id]
    }

    override suspend fun updateTeam(team: Team): Result<Unit> {
        _teams.value = _teams.value + (team.id to team)
        return Result.success(Unit)
    }

    override suspend fun joinTeam(userId: String, teamId: String): Result<Unit> {
        val team = _teams.value[teamId] ?: return Result.failure(Exception("Not found"))
        val updated = team.copy(memberIds = team.memberIds + userId)
        _teams.value = _teams.value + (teamId to updated)
        return Result.success(Unit)
    }

    override suspend fun leaveTeam(userId: String, teamId: String): Result<Unit> {
        val team = _teams.value[teamId] ?: return Result.failure(Exception("Not found"))
        val updated = team.copy(memberIds = team.memberIds - userId)
        _teams.value = _teams.value + (teamId to updated)
        return Result.success(Unit)
    }

    override suspend fun getInstallations(): List<Installation> {
        return _installations.value
    }

    override suspend fun rentInstallation(userId: String, installationId: String): Result<Unit> {
        val list = _installations.value.map {
            if (it.id == installationId) it.copy(available = false) else it
        }
        _installations.value = list
        return Result.success(Unit)
    }
}
