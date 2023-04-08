package com.walletconnect.sync.client

import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.sync.client.mapper.toClient
import com.walletconnect.sync.client.mapper.toClientError
import com.walletconnect.sync.common.model.Events
import com.walletconnect.sync.di.engineModule
import com.walletconnect.sync.engine.domain.SyncEngine
import kotlinx.coroutines.launch

class SyncProtocol : SyncInterface {
    private lateinit var syncEngine: SyncEngine

    companion object {
        val instance = SyncProtocol()
    }

    override fun initialize(params: Sync.Params.Init, onError: (Sync.Model.Error) -> Unit) {
        try {
            wcKoinApp.run {
                modules(
                    engineModule(),
                )
            }

            syncEngine = wcKoinApp.koin.get()
            syncEngine.setup()
        } catch (e: Exception) {
            onError(Sync.Model.Error(e))
        }
    }

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

    override fun getMessage(params: Sync.Params.GetMessage): String? = wrapWithEngineInitializationCheck() {
        TODO("Not yet implemented")
    }

    override fun register(params: Sync.Params.Register, onSuccess: () -> Unit, onError: (Sync.Model.Error) -> Unit) = protocolFunction(onError) {
        TODO("Not yet implemented")
    }

    override fun create(params: Sync.Params.Create, onSuccess: () -> Unit, onError: (Sync.Model.Error) -> Unit) = protocolFunction(onError) {
        TODO("Not yet implemented")
    }

    override fun set(params: Sync.Params.Set, onSuccess: () -> Unit, onError: (Sync.Model.Error) -> Unit) = protocolFunction(onError) {
        TODO("Not yet implemented")
    }

    override fun delete(params: Sync.Params.Delete, onSuccess: () -> Unit, onError: (Sync.Model.Error) -> Unit) = protocolFunction(onError) {
        TODO("Not yet implemented")
    }

    override fun getStores(params: Sync.Params.GetStores): Sync.Type.StoreMap? = wrapWithEngineInitializationCheck() {
        TODO("Not yet implemented")
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
}