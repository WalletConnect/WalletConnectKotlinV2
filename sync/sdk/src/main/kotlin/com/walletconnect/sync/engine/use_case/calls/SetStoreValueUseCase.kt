package com.walletconnect.sync.engine.use_case.calls

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.sync.common.exception.validateAccountId
import com.walletconnect.sync.common.json_rpc.SyncParams
import com.walletconnect.sync.common.json_rpc.SyncRpc
import com.walletconnect.sync.common.model.Store
import com.walletconnect.sync.storage.StoresStorageRepository
import com.walletconnect.util.generateId
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class SetStoreValueUseCase(private val storesRepository: StoresStorageRepository, private val jsonRpcInteractor: JsonRpcInteractorInterface) : SetUseCaseInterface {

    // https://github.com/WalletConnect/WalletConnectKotlinV2/issues/800 -> update params to have StoreKey and StoreValue
    override fun set(accountId: AccountId, store: Store, key: String, value: String, onSuccess: (Boolean) -> Unit, onFailure: (Throwable) -> Unit) {
        suspend fun publishSetRequest() {
            val storeTopic = runCatching { storesRepository.getStoreTopic(accountId, store) }.fold(onSuccess = { Topic(it) }, onFailure = { error -> return onFailure(error) })
            val setParams = SyncParams.SetParams(key, value)
            val payload = SyncRpc.SyncSet(id = generateId(), params = setParams)
            val irnParams = IrnParams(Tags.SYNC_SET, Ttl(MONTH_IN_SECONDS))

            jsonRpcInteractor.publishJsonRpcRequest(storeTopic, irnParams, payload, onSuccess = { onSuccess(true) }, onFailure = onFailure)
        }

        scope.launch {
            supervisorScope {
                validateAccountId(accountId) { error -> return@supervisorScope onFailure(error) }

                // Return false in onSuccess when the value was already set
                runCatching { storesRepository.getStoreValue(accountId, store, key) }
                    .onSuccess { (_, currentValue) -> if (value == currentValue) return@supervisorScope onSuccess(false) }

                // Return true in onSuccess when the value was upserted
                runCatching { storesRepository.upsertStoreValue(accountId, store, key, value) }.fold(
                    onSuccess = { publishSetRequest() },
                    onFailure = { error -> onFailure(error) }
                )
            }
        }
    }
}

internal interface SetUseCaseInterface {
    fun set(accountId: AccountId, store: Store, key: String, value: String, onSuccess: (Boolean) -> Unit, onFailure: (Throwable) -> Unit)
}
