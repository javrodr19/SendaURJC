package com.urjc.sendaurjc.data.local.dao

import androidx.room.*
import com.urjc.sendaurjc.data.local.entity.AlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: AlertEntity): Long

    @Query("SELECT * FROM critical_alerts WHERE processed = 0 ORDER BY timestamp ASC")
    fun observeUnprocessed(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM critical_alerts ORDER BY timestamp DESC")
    suspend fun getAll(): List<AlertEntity>

    @Query("UPDATE critical_alerts SET processed = 1 WHERE id = :id")
    suspend fun markProcessed(id: String)
}
