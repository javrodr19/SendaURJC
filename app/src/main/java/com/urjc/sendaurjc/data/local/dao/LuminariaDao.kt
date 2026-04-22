package com.urjc.sendaurjc.data.local.dao

import androidx.room.*
import com.urjc.sendaurjc.data.local.entity.LuminariaEntity
import com.urjc.sendaurjc.data.local.entity.LuminariaHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LuminariaDao {
    @Upsert
    suspend fun upsert(entity: LuminariaEntity)

    @Query("SELECT * FROM luminarias WHERE id = :id")
    fun observe(id: String): Flow<LuminariaEntity?>

    @Query("SELECT * FROM luminarias WHERE id = :id")
    suspend fun getById(id: String): LuminariaEntity?

    @Query("SELECT * FROM luminarias WHERE zoneId = :zoneId")
    suspend fun getByZone(zoneId: String): List<LuminariaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entity: LuminariaHistoryEntity)

    @Query("""
        SELECT * FROM luminaria_history
        WHERE (:luminariaId IS NULL OR luminariaId = :luminariaId)
        AND timestamp BETWEEN :from AND :to
        ORDER BY timestamp DESC
    """)
    suspend fun queryHistory(luminariaId: String?, from: Long, to: Long): List<LuminariaHistoryEntity>

    @Query("DELETE FROM luminarias WHERE id = :id")
    suspend fun delete(id: String)
}
