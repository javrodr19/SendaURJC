package com.urjc.sendaurjc.data.local.dao

import androidx.room.*
import com.urjc.sendaurjc.data.local.entity.IncidentEntity
import com.urjc.sendaurjc.data.local.entity.TicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: IncidentEntity)

    @Query("SELECT * FROM incidents ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents WHERE id = :id")
    suspend fun getById(id: String): IncidentEntity?

    @Query("UPDATE incidents SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE incidents SET ticketId = :ticketId WHERE id = :id")
    suspend fun linkTicket(id: String, ticketId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(entity: TicketEntity)

    @Query("SELECT * FROM tickets WHERE incidentId = :incidentId LIMIT 1")
    suspend fun getTicketByIncident(incidentId: String): TicketEntity?

    @Query("SELECT * FROM tickets ORDER BY createdAt DESC")
    suspend fun getAllTickets(): List<TicketEntity>
}
