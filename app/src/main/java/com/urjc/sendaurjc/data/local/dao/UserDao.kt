package com.urjc.sendaurjc.data.local.dao

import androidx.room.*
import com.urjc.sendaurjc.data.local.entity.TrustedContactEntity
import com.urjc.sendaurjc.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Upsert
    suspend fun upsert(entity: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observe(id: String): Flow<UserEntity?>

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun delete(id: String)

    @Upsert
    suspend fun upsertTrustedContact(entity: TrustedContactEntity)

    @Query("SELECT * FROM trusted_contacts WHERE userId = :userId LIMIT 1")
    suspend fun getTrustedContact(userId: String): TrustedContactEntity?
}
