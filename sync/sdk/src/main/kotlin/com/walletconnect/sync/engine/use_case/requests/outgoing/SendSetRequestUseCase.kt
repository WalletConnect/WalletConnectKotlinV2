package com.walletconnect.sync.engine.use_case.requests.outgoing

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.sync.common.json_rpc.SyncParams
import com.walletconnect.sync.common.json_rpc.SyncRpc
import com.walletconnect.sync.common.model.Store
import com.walletconnect.sync.storage.StoresStorageRepository
import com.walletconnect.util.generateId

internal class SendSetRequestUseCase(private val jsonRpcInteractor: JsonRpcInteractorInterface, private val storesRepository: StoresStorageRepository) {

    // https://github.com/WalletConnect/WalletConnectKotlinV2/issues/800 -> update params to have StoreKey and StoreValue
    suspend operator fun invoke(key: String, value: String, accountId: AccountId, store: Store, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        val storeTopic = runCatching { storesRepository.getStoreTopic(accountId, store) }.fold(onSuccess = { Topic(it) }, onFailure = { error -> return onError(error) })
        val setParams = SyncParams.SetParams(key, value)
        val payload = SyncRpc.SyncSet(id = generateId(), params = setParams)
        val irnParams = IrnParams(Tags.SYNC_SET, Ttl(MONTH_IN_SECONDS))

        jsonRpcInteractor.publishJsonRpcRequest(storeTopic, irnParams, payload, onSuccess = onSuccess, onFailure = onError)
    }
}