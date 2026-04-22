package com.urjc.sendaurjc.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.urjc.sendaurjc.data.local.dao.*
import com.urjc.sendaurjc.data.local.entity.*

@Database(
    entities = [
        LuminariaEntity::class,
        LuminariaHistoryEntity::class,
        ZoneEntity::class,
        UserEntity::class,
        TrustedContactEntity::class,
        IncidentEntity::class,
        TicketEntity::class,
        AlertEntity::class,
        CompanionRequestEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun luminariaDao(): LuminariaDao
    abstract fun zoneDao(): ZoneDao
    abstract fun userDao(): UserDao
    abstract fun incidentDao(): IncidentDao
    abstract fun alertDao(): AlertDao
    abstract fun companionDao(): CompanionDao
}
