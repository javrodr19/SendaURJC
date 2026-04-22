package com.urjc.sendaurjc.domain.repository

import com.urjc.sendaurjc.domain.model.Activity
import com.urjc.sendaurjc.domain.model.Installation
import com.urjc.sendaurjc.domain.model.Team
import kotlinx.coroutines.flow.Flow

interface CampusRepository {
    // Activities
    suspend fun createActivity(activity: Activity): Result<Unit>
    suspend fun getActivity(id: String): Activity?
    fun getAllActivities(): Flow<List<Activity>>
    suspend fun joinActivity(userId: String, activityId: String): Result<Unit>
    
    // Teams
    suspend fun createTeam(team: Team): Result<Unit>
    suspend fun getTeam(id: String): Team?
    suspend fun updateTeam(team: Team): Result<Unit>
    suspend fun joinTeam(userId: String, teamId: String): Result<Unit>
    suspend fun leaveTeam(userId: String, teamId: String): Result<Unit>

    // Installations
    suspend fun getInstallations(): List<Installation>
    suspend fun rentInstallation(userId: String, installationId: String): Result<Unit>
}
