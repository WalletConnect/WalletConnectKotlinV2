package com.walletconnect.sign.json_rpc.domain

import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import kotlinx.coroutines.supervisorScope

internal class DeleteRequestByIdUseCase(private val jsonRpcHistory: JsonRpcHistory) {

    suspend operator fun invoke(id: Long) {
        supervisorScope {
            jsonRpcHistory.deleteRecordById(id)
        }
    }
}