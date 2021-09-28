package org.walletconnect.walletconnectv2.relay

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.BackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.tinder.streamadapter.coroutines.CoroutinesStreamAdapterFactory
import okhttp3.OkHttpClient
import org.walletconnect.walletconnectv2.clientsync.PreSettlementPairing
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.common.network.adapters.ExpiryAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.JSONObjectAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.TopicAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.TtlAdapter
import org.walletconnect.walletconnectv2.common.toRelayPublishRequest
import org.walletconnect.walletconnectv2.relay.data.RelayService
import org.walletconnect.walletconnectv2.util.adapters.FlowStreamAdapter
import java.util.concurrent.TimeUnit

class WakuRelayRepository internal constructor(private val useTLs: Boolean, private val hostName: String, private val port: Int) {
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
    private val scarlet by lazy {
        Scarlet.Builder()
            .backoffStrategy(getBackoffStrategy(5))
            .webSocketFactory(okHttpClient.newWebSocketFactory(getServerUrl()))
            .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi))
            .addStreamAdapterFactory(FlowStreamAdapter.Factory())
            .build()
    }
    private val relay: RelayService by lazy { scarlet.create() }
    //endregion

    val events = relay.observeEvents()
    val publishResponse = relay.observePublishResponse()
    val subscribeResponse = relay.observeSubscribeResponse()
    val subscriptionRequest = relay.observeSubscriptionRequest()
    val unsubscribeResponse = relay.observeUnsubscribeResponse()

    fun publish(topic: Topic, preSettlementPairingApproval: PreSettlementPairing.Approve) {
        val publishRequest = preSettlementPairingApproval.toRelayPublishRequest(2, topic, moshi)

        relay.publishRequest(publishRequest)
    }

    private fun getServerUrl(): String = (if (useTLs) "wss" else "ws") + "://$hostName:$port"

    companion object {
        private const val defaultRemotePort = 443

        fun initRemote(useTLs: Boolean = false, hostName: String, port: Int = defaultRemotePort) =
            WakuRelayRepository(useTLs, hostName, port)
    }

    @Suppress("SameParameterValue")
    private fun getBackoffStrategy(retryInMins: Long): BackoffStrategy =
        object : BackoffStrategy {
            override fun backoffDurationMillisAt(retryCount: Int): Long = TimeUnit.MINUTES.toMillis(retryInMins)
        }
}

