package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toEngineDO
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import com.walletconnect.utils.isSequenceValid
import kotlinx.coroutines.supervisorScope

internal class GetSessionsUseCase(
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val sessionStorageRepository: SessionStorageRepository,
    private val selfAppMetaData: AppMetaData
) : GetSessionsUseCaseInterface {

    override suspend fun getListOfSettledSessions(): List<EngineDO.Session> = supervisorScope {
        return@supervisorScope sessionStorageRepository.getListOfSessionVOsWithoutMetadata()
            .filter { session -> session.isAcknowledged && session.expiry.isSequenceValid() }
            .map { session -> session.copy(selfAppMetaData = selfAppMetaData, peerAppMetaData = metadataStorageRepository.getByTopicAndType(session.topic, AppMetaDataType.PEER)) }
            .map { session -> session.toEngineDO() }
    }
}

internal interface GetSessionsUseCaseInterface {
    suspend fun getListOfSettledSessions(): List<EngineDO.Session>
}