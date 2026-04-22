package com.urjc.sendaurjc.di

import com.urjc.sendaurjc.data.repository.*
import com.urjc.sendaurjc.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindLuminariaRepository(impl: LuminariaRepositoryImpl): LuminariaRepository

    @Binds @Singleton
    abstract fun bindZoneRepository(impl: ZoneRepositoryImpl): ZoneRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds @Singleton
    abstract fun bindRouteRepository(impl: RouteRepositoryImpl): RouteRepository

    @Binds @Singleton
    abstract fun bindIncidentRepository(impl: IncidentRepositoryImpl): IncidentRepository

    @Binds @Singleton
    abstract fun bindAlertRepository(impl: AlertRepositoryImpl): AlertRepository

    @Binds @Singleton
    abstract fun bindCompanionRepository(impl: CompanionRepositoryImpl): CompanionRepository

    @Binds @Singleton
    abstract fun bindCampusRepository(impl: CampusRepositoryImpl): CampusRepository
}
