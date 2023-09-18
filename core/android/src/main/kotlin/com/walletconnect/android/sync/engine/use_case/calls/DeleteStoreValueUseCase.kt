package com.walletconnect.android.sync.engine.use_case.calls

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.android.sync.common.exception.validateAccountId
import com.walletconnect.android.sync.common.json_rpc.SyncParams
import com.walletconnect.android.sync.common.json_rpc.SyncRpc
import com.walletconnect.android.sync.common.model.Store
import com.walletconnect.android.sync.storage.StoresStorageRepository
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class DeleteStoreValueUseCase(private val storesRepository: StoresStorageRepository, private val jsonRpcInteractor: JsonRpcInteractorInterface) : DeleteUseCaseInterface {

    // https://github.com/WalletConnect/WalletConnectKotlinV2/issues/800 -> update params to have StoreKey and StoreValue
    override fun delete(accountId: AccountId, store: Store, key: String, onSuccess: (Boolean) -> Unit, onError: (Throwable) -> Unit) {
        suspend fun publishDeleteRequest() {
            val storeTopic = runCatching { storesRepository.getStoreTopic(accountId, store) }.fold(onSuccess = { Topic(it) }, onFailure = { error -> return onError(error) })
            val deleteParams = SyncParams.DeleteParams(key)
            val payload = SyncRpc.SyncDelete(params = deleteParams, topic = storeTopic.value)
            val irnParams = IrnParams(Tags.SYNC_DELETE, Ttl(MONTH_IN_SECONDS))

            jsonRpcInteractor.publishJsonRpcRequest(storeTopic, irnParams, payload, onSuccess = { onSuccess(true) }, onFailure = onError)
        }

        scope.launch {
            supervisorScope {
                validateAccountId(accountId) { error -> return@supervisorScope onError(error) }

                // Return false in onSuccess when the value was not in storage
                runCatching { storesRepository.getStoreValue(accountId, store, key) }.getOrElse { return@supervisorScope onSuccess(false) }

                // Return true in onSuccess when the value was deleted
                runCatching { storesRepository.deleteStoreValue(accountId, store, key) }.fold(
                    onSuccess = { publishDeleteRequest() },
                    onFailure = { error -> onError(error) }
                )
            }
        }
    }
}

internal interface DeleteUseCaseInterface {
    fun delete(accountId: AccountId, store: Store, key: String, onSuccess: (Boolean) -> Unit, onError: (Throwable) -> Unit)
}
