package com.walletconnect.sign.json_rpc.domain

import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import kotlinx.coroutines.supervisorScope

internal class DeleteRequestByIdUseCase(
    private val jsonRpcHistory: JsonRpcHistory,
    private val verifyContextStorageRepository: VerifyContextStorageRepository
) {

    suspend operator fun invoke(id: Long) {
        supervisorScope {
            jsonRpcHistory.deleteRecordById(id)
            verifyContextStorageRepository.delete(id)
        }
    }
}