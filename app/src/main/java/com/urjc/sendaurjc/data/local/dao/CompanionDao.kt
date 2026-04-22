package com.urjc.sendaurjc.data.local.dao

import androidx.room.*
import com.urjc.sendaurjc.data.local.entity.CompanionRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanionDao {
    @Upsert
    suspend fun upsert(entity: CompanionRequestEntity)

    @Query("SELECT * FROM companion_requests WHERE status = 'PENDING' ORDER BY scheduledTime ASC")
    fun observePending(): Flow<List<CompanionRequestEntity>>

    @Query("SELECT * FROM companion_requests WHERE requesterId = :userId ORDER BY scheduledTime DESC")
    fun observeByRequester(userId: String): Flow<List<CompanionRequestEntity>>

    @Query("SELECT * FROM companion_requests WHERE id = :id")
    suspend fun getById(id: String): CompanionRequestEntity?

    @Query("UPDATE companion_requests SET status = :status, volunteerId = :volunteerId WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, volunteerId: String?)
}
