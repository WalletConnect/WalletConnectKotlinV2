package com.walletconnect.android.sync.client

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.android.sync.common.model.StoreMap
import com.walletconnect.android.sync.di.commonModule
import com.walletconnect.android.sync.di.engineModule
import com.walletconnect.android.sync.di.jsonRpcModule
import com.walletconnect.android.sync.di.syncStorageModule
import com.walletconnect.android.sync.engine.domain.SyncEngine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal class SyncProtocol : SyncInterface {
    private lateinit var syncEngine: SyncEngine

    private val _onSyncUpdateEvents: MutableSharedFlow<Events.OnSyncUpdate> = MutableSharedFlow()
    override val onSyncUpdateEvents: SharedFlow<Events.OnSyncUpdate> = _onSyncUpdateEvents.asSharedFlow()

    companion object {
        val instance = SyncProtocol()
    }

    override fun initialize(onError: (Core.Model.Error) -> Unit) {
        try {
            wcKoinApp.run {
                modules(
                    jsonRpcModule(),
                    commonModule(),
                    syncStorageModule(),
                    engineModule(),
                )
            }

            syncEngine = wcKoinApp.koin.get()
            syncEngine.setup()

            scope.launch {
                syncEngine.events.collect { event ->
                    when (event) {
                        is Events.OnSyncUpdate -> _onSyncUpdateEvents.emit(event)
                    }
                }
            }
        } catch (e: Exception) {
            onError(Core.Model.Error(e))
        }
    }

    @Throws(IllegalStateException::class)
    override fun getMessage(params: Sync.Params.GetMessage): String = wrapWithEngineInitializationCheck {
        syncEngine.getMessage(params.accountId)
    }

    @Throws(IllegalStateException::class)
    override fun register(params: Sync.Params.Register, onSuccess: () -> Unit, onError: (Core.Model.Error) -> Unit) = protocolFunction(onError) {
        syncEngine.register(params.accountId, params.signature.s, params.signatureType, onSuccess) { error -> onError(Core.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun isRegistered(params: Sync.Params.IsRegistered, onSuccess: (Boolean) -> Unit, onError: (Core.Model.Error) -> Unit) = protocolFunction(onError) {
        syncEngine.isRegistered(params.accountId, onSuccess) { error -> onError(Core.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun create(params: Sync.Params.Create, onSuccess: () -> Unit, onError: (Core.Model.Error) -> Unit) = protocolFunction(onError) {
        syncEngine.create(params.accountId, params.store, onSuccess) { error -> onError(Core.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun set(params: Sync.Params.Set, onSuccess: (Boolean) -> Unit, onError: (Core.Model.Error) -> Unit) = protocolFunction(onError) {
        syncEngine.set(params.accountId, params.store, params.key, params.value, onSuccess) { error -> onError(Core.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun delete(params: Sync.Params.Delete, onSuccess: (Boolean) -> Unit, onError: (Core.Model.Error) -> Unit) = protocolFunction(onError) {
        syncEngine.delete(params.accountId, params.store, params.key, onSuccess) { error -> onError(Core.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun getStores(params: Sync.Params.GetStores): StoreMap? = wrapWithEngineInitializationCheck {
        syncEngine.getStores(params.accountId)
    }

    @Throws(IllegalStateException::class)
    private fun <R> wrapWithEngineInitializationCheck(block: () -> R): R {
        check(::syncEngine.isInitialized) { "SyncClient needs to be initialized first using the initialize function" }
        return block()
    }

    private fun wrapWithRunCatching(onError: (Core.Model.Error) -> Unit, block: () -> Unit) = runCatching(block).onFailure { error -> onError(Core.Model.Error(error)) }

    @Throws(IllegalStateException::class)
    private fun protocolFunction(onError: (Core.Model.Error) -> Unit, block: () -> Unit) {
        wrapWithEngineInitializationCheck() {
            wrapWithRunCatching(onError) { block() }
        }
    }
}