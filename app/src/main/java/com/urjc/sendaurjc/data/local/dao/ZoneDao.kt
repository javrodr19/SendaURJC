package com.urjc.sendaurjc.data.local.dao

import androidx.room.*
import com.urjc.sendaurjc.data.local.entity.ZoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ZoneDao {
    @Upsert
    suspend fun upsert(entity: ZoneEntity)

    @Query("SELECT * FROM zones WHERE id = :id")
    fun observe(id: String): Flow<ZoneEntity?>

    @Query("SELECT * FROM zones WHERE id = :id")
    suspend fun getById(id: String): ZoneEntity?

    @Query("SELECT * FROM zones")
    suspend fun getAll(): List<ZoneEntity>

    @Query("UPDATE zones SET trafficIndex = :index WHERE id = :zoneId")
    suspend fun updateTrafficIndex(zoneId: String, index: Int)

    @Query("UPDATE zones SET hasPartialCoverage = :partial WHERE id = :zoneId")
    suspend fun updatePartialCoverage(zoneId: String, partial: Boolean)

    @Query("UPDATE zones SET dataQuality = :quality WHERE id = :zoneId")
    suspend fun updateDataQuality(zoneId: String, quality: String)
}
