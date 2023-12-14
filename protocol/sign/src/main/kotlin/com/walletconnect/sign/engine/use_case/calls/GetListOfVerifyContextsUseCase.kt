package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toEngineDO

internal class GetListOfVerifyContextsUseCase(private val verifyContextStorageRepository: VerifyContextStorageRepository) : GetListOfVerifyContextsUseCaseInterface {
    override suspend fun getListOfVerifyContexts(): List<EngineDO.VerifyContext> = verifyContextStorageRepository.getAll().map { verifyContext -> verifyContext.toEngineDO() }
}

internal interface GetListOfVerifyContextsUseCaseInterface {
    suspend fun getListOfVerifyContexts(): List<EngineDO.VerifyContext>
}