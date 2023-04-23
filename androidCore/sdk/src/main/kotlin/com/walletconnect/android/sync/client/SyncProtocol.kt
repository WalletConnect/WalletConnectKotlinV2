package com.walletconnect.android.sync.client

import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.sync.client.mapper.asString
import com.walletconnect.android.sync.client.mapper.toClient
import com.walletconnect.android.sync.client.mapper.toClientError
import com.walletconnect.android.sync.client.mapper.toCommon
import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.android.sync.di.commonModule
import com.walletconnect.android.sync.di.engineModule
import com.walletconnect.android.sync.di.syncStorageModule
import com.walletconnect.android.sync.engine.domain.SyncEngine
import kotlinx.coroutines.launch

internal class SyncProtocol : SyncInterface {
    private lateinit var syncEngine: SyncEngine

    companion object {
        val instance = SyncProtocol()
    }

    override fun initialize(
        params: Sync.Params.Init,
        onError: (Sync.Model.Error) -> Unit,
    ) {
        try {
            wcKoinApp.run {
                modules(
                    commonModule(),
                    syncStorageModule(),
                    engineModule(),
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
        syncEngine.getMessage(params.accountId.toCommon())
    }

    @Throws(IllegalStateException::class)
    override fun register(
        params: Sync.Params.Register,
        onSuccess: () -> Unit,
        onError: (Sync.Model.Error) -> Unit,
    ) = protocolFunction(onError) {
        syncEngine.register(
            params.accountId.toCommon(),
            params.signature.asString(),
            params.signatureType,
            onSuccess
        ) { error -> onError(Sync.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun create(
        params: Sync.Params.Create,
        onSuccess: () -> Unit,
        onError: (Sync.Model.Error) -> Unit,
    ) = protocolFunction(onError) {
        syncEngine.create(params.accountId.toCommon(), params.store.toCommon(), onSuccess) { error -> onError(Sync.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun set(
        params: Sync.Params.Set,
        onSuccess: (Boolean) -> Unit,
        onError: (Sync.Model.Error) -> Unit,
    ) = protocolFunction(onError) {
        syncEngine.set(
            params.accountId.toCommon(),
            params.store.toCommon(),
            params.key,
            params.value,
            onSuccess
        ) { error -> onError(Sync.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun delete(
        params: Sync.Params.Delete,
        onSuccess: (Boolean) -> Unit,
        onError: (Sync.Model.Error) -> Unit,
    ) = protocolFunction(onError) {
        syncEngine.delete(params.accountId.toCommon(), params.store.toCommon(), params.key, onSuccess) { error ->
            onError(
                Sync.Model.Error(
                    error
                )
            )
        }
    }

    @Throws(IllegalStateException::class)
    override fun getStores(params: Sync.Params.GetStores): Sync.Type.StoreMap? =
        wrapWithEngineInitializationCheck() {
            syncEngine.getStores(params.accountId.toCommon())?.toClient()
        }

    @Throws(IllegalStateException::class)
    private fun <R> wrapWithEngineInitializationCheck(block: () -> R): R {
        check(::syncEngine.isInitialized) {
            "ChatClient needs to be initialized first using the initialize function"
        }
        return block()
    }

    private fun wrapWithRunCatching(onError: (Sync.Model.Error) -> Unit, block: () -> Unit) = runCatching(block).onFailure { error ->
        onError(
            Sync.Model.Error(error)
        )
    }

    @Throws(IllegalStateException::class)
    private fun protocolFunction(onError: (Sync.Model.Error) -> Unit, block: () -> Unit) {
        wrapWithEngineInitializationCheck() {
            wrapWithRunCatching(onError) { block() }
        }
    }
}