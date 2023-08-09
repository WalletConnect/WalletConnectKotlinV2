@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.notify.engine.sync.use_case.GetMessagesFromHistoryUseCase
import com.walletconnect.notify.engine.sync.use_case.SetupSyncInNotifyUseCase
import kotlinx.coroutines.launch

internal class EnableSyncUseCase(
    private val setupSyncInNotifyUseCase: SetupSyncInNotifyUseCase,
    private val getMessagesFromHistoryUseCase: GetMessagesFromHistoryUseCase,
): EnableSyncUseCaseInterface {

    override suspend fun enableSync(account: String, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        setupSyncInNotifyUseCase(AccountId(account), onSign, onSuccess = {
            scope.launch { getMessagesFromHistoryUseCase(AccountId(account), onSuccess, onFailure) }
        }, onFailure)
    }
}

internal interface EnableSyncUseCaseInterface {
    suspend fun enableSync(account: String, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}