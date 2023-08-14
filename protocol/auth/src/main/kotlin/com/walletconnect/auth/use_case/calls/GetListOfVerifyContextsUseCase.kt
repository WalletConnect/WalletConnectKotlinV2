package com.walletconnect.auth.use_case.calls

import com.walletconnect.android.internal.common.storage.VerifyContextStorageRepository
import com.walletconnect.android.verify.data.model.VerifyContext

internal class GetListOfVerifyContextsUseCase(private val verifyContextStorageRepository: VerifyContextStorageRepository) : GetListOfVerifyContextsUseCaseInterface {
    override suspend fun getListOfVerifyContext(): List<VerifyContext> = verifyContextStorageRepository.getAll()
}

internal interface GetListOfVerifyContextsUseCaseInterface {
     suspend fun getListOfVerifyContext(): List<VerifyContext>
}
