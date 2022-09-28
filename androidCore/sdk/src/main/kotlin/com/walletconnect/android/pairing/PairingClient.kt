package com.walletconnect.android.pairing

import com.walletconnect.android.Core
import com.walletconnect.android.common.crypto.KeyManagementRepository
import com.walletconnect.android.common.di.AndroidCommonDITags
import com.walletconnect.android.common.di.androidApiCryptoModule
import com.walletconnect.android.common.di.commonModule
import com.walletconnect.android.common.di.pairingModule
import com.walletconnect.android.common.model.metadata.PeerMetaData
import com.walletconnect.android.common.model.pairing.Expiry
import com.walletconnect.android.common.model.pairing.Pairing
import com.walletconnect.android.common.model.sync.WCRequest
import com.walletconnect.android.common.pairing.PairingStorageRepository
import com.walletconnect.android.common.wcKoinApp
import com.walletconnect.android.common.exception.MethodAlreadyRegistered
import com.walletconnect.android.common.json_rpc.BaseJsonRpcInteractor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named

internal object PairingClient : PairingInterface {
    private lateinit var _selfMetaData: Core.Model.AppMetaData
    override val selfMetaData: Core.Model.AppMetaData
        get() = _selfMetaData

    private val storage: PairingStorageRepository by lazy { wcKoinApp.koin.get() }
    private val relay: BaseJsonRpcInteractor by lazy { wcKoinApp.koin.get() }
    private val crypto: KeyManagementRepository by lazy { wcKoinApp.koin.get() } //todo: dummy. doesn't work currenlty. Need help setting up

    override fun initialize(metaData: Core.Model.AppMetaData) {
        _selfMetaData = metaData
        wcKoinApp.run {
            modules(coreStorageModule(), pairingModule())
        }
    }

    override fun ping(ping: Core.Params.Ping, sessionPing: Core.Listeners.SessionPing?) {
        TODO("Not yet implemented")
    }

    override fun create(onPairingCreated: (String) -> Unit, onError: (Core.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun pair(pair: Core.Params.Pair, onError: (Core.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getPairings(): List<Pairing> {
        TODO("Not yet implemented")
    }

    override fun disconnect(topic: String) {
        TODO("Not yet implemented")
    }

    override fun activate(topic: String, onError: (Core.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun updateExpiry(topic: String, expiry: Expiry, onError: (Core.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun updateMetadata(topic: String, metadata: PeerMetaData, onError: (Core.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }

    private val methodsToCallbacks: MutableMap<String, (topic: String, request: WCRequest) -> Unit> = mutableMapOf()

    @Throws(MethodAlreadyRegistered::class)
    override fun register(method: String, onMethod: (topic: String, request: WCRequest) -> Unit) {
        if (methodsToCallbacks.containsKey(method)) throw MethodAlreadyRegistered("Method: $method already registered")

        methodsToCallbacks[method] = onMethod
    }
}