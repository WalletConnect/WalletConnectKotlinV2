@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.notify.engine.sync.use_case.GetMessagesFromHistoryUseCase
import com.walletconnect.notify.engine.sync.use_case.SetupSyncInNotifyUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class RegisterUseCase(
    private val keyserverUrl: String,
    private val identitiesInteractor: IdentitiesInteractor,
    private val setupSyncInNotifyUseCase: SetupSyncInNotifyUseCase,
    private val getMessagesFromHistoryUseCase: GetMessagesFromHistoryUseCase,
) : RegisterUseCaseInterface {

    override suspend fun register(account: String, onSign: (String) -> Cacao.Signature?, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        identitiesInteractor.registerIdentity(AccountId(account), keyserverUrl, onSign).fold(
            onFailure = { error -> onFailure(error) },
            onSuccess = { identityPublicKey ->
//                setupSyncInNotifyUseCase(
//                    accountId = AccountId(account),
//                    onSign = onSign,
//                    onSuccess = {
//                        scope.launch {
//                            getMessagesFromHistoryUseCase(AccountId(account), {
//                                onSuccess(identityPublicKey.keyAsHex)
//                            }, onFailure)
//                        }
//                    },
//                    onError = onFailure
//                )
            }
        )
    }
}

internal interface RegisterUseCaseInterface {
    suspend fun register(account: String, onSign: (String) -> Cacao.Signature?, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit)
}