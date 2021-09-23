package org.walletconnect.walletconnectv2.relay

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.tinder.streamadapter.coroutines.CoroutinesStreamAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import okhttp3.OkHttpClient
import org.walletconnect.walletconnectv2.clientsync.PreSettlementPairing
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.common.network.adapters.ExpiryAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.JSONObjectAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.TopicAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.TtlAdapter
import org.walletconnect.walletconnectv2.common.toRelayPublishRequest
import org.walletconnect.walletconnectv2.crypto.CryptoManager
import org.walletconnect.walletconnectv2.crypto.KeyChain
import org.walletconnect.walletconnectv2.crypto.managers.LazySodiumCryptoManager
import org.walletconnect.walletconnectv2.relay.data.RelayService
import java.util.concurrent.TimeUnit

class DefaultRelayRepository internal constructor(private val useTLs: Boolean, private val hostName: String, private val port: Int) {
    //region Move to DI module
    private val okHttpClient = OkHttpClient.Builder()
        .writeTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(500, TimeUnit.MILLISECONDS)
        .build()
    private val moshi: Moshi = Moshi.Builder()
        .add(TopicAdapter)
        .add(ExpiryAdapter)
        .add(TtlAdapter)
        .add(JSONObjectAdapter)
        .add(KotlinJsonAdapterFactory())
        .build()
    private val scarlet = Scarlet.Builder()
        .webSocketFactory(okHttpClient.newWebSocketFactory(getServerUrl()))
        .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi))
        .addStreamAdapterFactory(CoroutinesStreamAdapterFactory())
        .build()
    private val relay: RelayService = scarlet.create()
    private val keyChain = object: KeyChain {
        val mapOfKeys = mutableMapOf<String, String>()

        override fun setKey(key: String, value: String) {
            mapOfKeys[key] = value
        }

        override fun getKey(key: String): String {
            return mapOfKeys[key]!!
        }
    }
    private val crypto: CryptoManager = LazySodiumCryptoManager(keyChain)
    //endregion

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.IO)

    val pairingResponse = relay.observeSubscriptionResponse().receiveAsFlow().shareIn(scope, SharingStarted.Lazily)

    fun sendPairingRequest(preSettlementPairingApprove: PreSettlementPairing.Approve) {
        val keyPairPublicKey = crypto.generateKeyPair()
        preSettlementPairingApprove.toRelayPublishRequest(2, Topic(keyPairPublicKey.key), moshi)
    }

    private fun getServerUrl(): String = (if (useTLs) "wss" else "ws")+"://$hostName:$port"

    companion object {
        private const val defaultRemotePort = 443

        fun initRemote(useTLs: Boolean = false, hostName: String, port: Int = defaultRemotePort) =
            DefaultRelayRepository(useTLs, hostName, port)
    }
}

