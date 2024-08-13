package com.walletconnect.auth.use_case.calls

import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.android.verify.model.VerifyContext
import kotlinx.coroutines.supervisorScope

internal class GetVerifyContextUseCase(private val verifyContextStorageRepository: VerifyContextStorageRepository) : GetVerifyContextUseCaseInterface {
    override suspend fun getVerifyContext(id: Long): VerifyContext? = supervisorScope { verifyContextStorageRepository.get(id) }
}

internal interface GetVerifyContextUseCaseInterface {
    suspend fun getVerifyContext(id: Long): VerifyContext?
}