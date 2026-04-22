package com.urjc.sendaurjc.di

import android.content.Context
import androidx.room.Room
import com.urjc.sendaurjc.data.local.AppDatabase
import com.urjc.sendaurjc.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "senda_urjc.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideLuminariaDao(db: AppDatabase): LuminariaDao = db.luminariaDao()
    @Provides fun provideZoneDao(db: AppDatabase): ZoneDao = db.zoneDao()
    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    @Provides fun provideIncidentDao(db: AppDatabase): IncidentDao = db.incidentDao()
    @Provides fun provideAlertDao(db: AppDatabase): AlertDao = db.alertDao()
    @Provides fun provideCompanionDao(db: AppDatabase): CompanionDao = db.companionDao()
}
