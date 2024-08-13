package com.walletconnect.auth.use_case.calls

import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.android.verify.model.VerifyContext
import kotlinx.coroutines.supervisorScope

internal class GetListOfVerifyContextsUseCase(private val verifyContextStorageRepository: VerifyContextStorageRepository) : GetListOfVerifyContextsUseCaseInterface {
    override suspend fun getListOfVerifyContext(): List<VerifyContext> = supervisorScope { verifyContextStorageRepository.getAll() }
}

internal interface GetListOfVerifyContextsUseCaseInterface {
    suspend fun getListOfVerifyContext(): List<VerifyContext>
}
