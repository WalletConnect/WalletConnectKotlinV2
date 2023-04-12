package com.walletconnect.sync.client

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.sync.client.mapper.toClient
import com.walletconnect.sync.client.mapper.toClientError
import com.walletconnect.sync.client.mapper.toCommon
import com.walletconnect.sync.common.exception.InvalidAccountIdException
import com.walletconnect.sync.common.model.Events
import com.walletconnect.sync.di.commonModule
import com.walletconnect.sync.di.engineModule
import com.walletconnect.sync.di.storageModule
import com.walletconnect.sync.engine.domain.SyncEngine
import kotlinx.coroutines.launch

internal class SyncProtocol : SyncInterface {
    private lateinit var syncEngine: SyncEngine

    companion object {
        val instance = SyncProtocol()
    }

    override fun initialize(params: Sync.Params.Init, onError: (Sync.Model.Error) -> Unit) {
        try {
            wcKoinApp.run {
                modules(
                    commonModule(),
                    engineModule(),
                    storageModule(),
                )
            }

            syncEngine = wcKoinApp.koin.get()
            syncEngine.setup()
        } catch (e: Exception) {
            onError(Sync.Model.Error(e))
        }
    }

    @Throws(IllegalStateException::class)
    override fun setSyncDelegate(delegate: SyncInterface.SyncDelegate): Unit = wrapWithEngineInitializationCheck() {
        scope.launch {
            syncEngine.events.collect { event ->
                when (event) {
                    is Events.OnSyncUpdate -> delegate.onSyncUpdate(event.toClient())
                    is ConnectionState -> delegate.onConnectionStateChange(event.toClient())
                    is SDKError -> delegate.onError(event.toClientError())
                }
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun getMessage(params: Sync.Params.GetMessage): String = wrapWithEngineInitializationCheck() {
        validateAccountId(params.accountId)
        syncEngine.getMessage(params.accountId)
    }

    @Throws(IllegalStateException::class)
    override fun register(params: Sync.Params.Register, onSuccess: () -> Unit, onError: (Sync.Model.Error) -> Unit) = protocolFunction(onError) {
        validateAccountId(params.accountId)
        syncEngine.register(params.accountId, params.signature.toCommon(), onSuccess) { error -> onError(Sync.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun create(params: Sync.Params.Create, onSuccess: () -> Unit, onError: (Sync.Model.Error) -> Unit) = protocolFunction(onError) {
        validateAccountId(params.accountId)
        syncEngine.create(params.accountId, params.store.toCommon())
    }

    @Throws(IllegalStateException::class)
    override fun set(params: Sync.Params.Set, onSuccess: () -> Unit, onError: (Sync.Model.Error) -> Unit) = protocolFunction(onError) {
        validateAccountId(params.accountId)
        syncEngine.set(params.accountId, params.store.toCommon(), params.key, params.value)
    }

    @Throws(IllegalStateException::class)
    override fun delete(params: Sync.Params.Delete, onSuccess: () -> Unit, onError: (Sync.Model.Error) -> Unit) = protocolFunction(onError) {
        validateAccountId(params.accountId)
        syncEngine.delete(params.accountId, params.store.toCommon(), params.key)
    }

    @Throws(IllegalStateException::class)
    override fun getStores(params: Sync.Params.GetStores): Sync.Type.StoreMap? = wrapWithEngineInitializationCheck() {
        validateAccountId(params.accountId)
        syncEngine.getStores(params.accountId)?.toClient()
    }

    @Throws(IllegalStateException::class)
    private fun <R> wrapWithEngineInitializationCheck(block: () -> R): R {
        check(::syncEngine.isInitialized) {
            "ChatClient needs to be initialized first using the initialize function"
        }
        return block()
    }

    private fun wrapWithRunCatching(onError: (Sync.Model.Error) -> Unit, block: () -> Unit) = runCatching(block).onFailure { error -> onError(Sync.Model.Error(error)) }

    @Throws(IllegalStateException::class)
    private fun protocolFunction(onError: (Sync.Model.Error) -> Unit, block: () -> Unit) {
        wrapWithEngineInitializationCheck() {
            wrapWithRunCatching(onError) { block() }
        }
    }

    private fun validateAccountId(accountId: AccountId) = if (!accountId.isValid()) throw InvalidAccountIdException(accountId) else Unit
}