package com.urjc.sendaurjc.data.repository

import com.urjc.sendaurjc.data.local.dao.CompanionDao
import com.urjc.sendaurjc.data.local.entity.CompanionRequestEntity
import com.urjc.sendaurjc.domain.model.*
import com.urjc.sendaurjc.domain.repository.CompanionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanionRepositoryImpl @Inject constructor(
    private val dao: CompanionDao
) : CompanionRepository {

    override suspend fun requestCompanion(
        requesterId: String,
        requesterName: String,
        origin: GeoPoint,
        destination: GeoPoint,
        scheduledTime: Long
    ): Result<CompanionRequest> = runCatching {
        val request = CompanionRequest(
            id = UUID.randomUUID().toString(),
            requesterId = requesterId,
            requesterName = requesterName,
            volunteerId = null,
            origin = origin,
            destination = destination,
            scheduledTime = scheduledTime,
            status = CompanionStatus.PENDING
        )
        dao.upsert(CompanionRequestEntity.fromDomain(request))
        request
    }

    override suspend fun acceptRequest(requestId: String, volunteerId: String): Result<CompanionRequest> =
        runCatching {
            val entity = requireNotNull(dao.getById(requestId)) { "Request not found" }
            dao.updateStatus(requestId, CompanionStatus.ACCEPTED.name, volunteerId)
            entity.copy(status = CompanionStatus.ACCEPTED.name, volunteerId = volunteerId).toDomain()
        }

    override suspend fun rejectRequest(requestId: String, volunteerId: String): Result<Unit> =
        runCatching {
            requireNotNull(dao.getById(requestId)) { "Request not found" }
            dao.updateStatus(requestId, CompanionStatus.REJECTED.name, volunteerId)
        }

    override fun observePendingRequests(): Flow<List<CompanionRequest>> =
        dao.observePending().map { list -> list.map { it.toDomain() } }

    override fun observeMyRequests(userId: String): Flow<List<CompanionRequest>> =
        dao.observeByRequester(userId).map { list -> list.map { it.toDomain() } }

    override suspend fun getRequest(id: String): CompanionRequest? =
        dao.getById(id)?.toDomain()
}
