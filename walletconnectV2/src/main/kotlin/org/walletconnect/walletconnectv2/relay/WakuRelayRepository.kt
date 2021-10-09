package org.walletconnect.walletconnectv2.relay

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.utils.getRawType
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import okhttp3.OkHttpClient
import org.json.JSONObject
import org.walletconnect.walletconnectv2.clientsync.PreSettlementPairing
import org.walletconnect.walletconnectv2.common.*
import org.walletconnect.walletconnectv2.common.network.adapters.*
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
        .addLast { type, _, _ ->
            when (type.getRawType().name) {
                Expiry::class.qualifiedName -> ExpiryAdapter
                JSONObject::class.qualifiedName -> JSONObjectAdapter
                SubscriptionId::class.qualifiedName -> SubscriptionIdAdapter
                Topic::class.qualifiedName -> TopicAdapter
                Ttl::class.qualifiedName -> TtlAdapter
                else -> null
            }
        }
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val scarlet by lazy {
        Scarlet.Builder()
            .backoffStrategy(LinearBackoffStrategy(TimeUnit.MINUTES.toMillis(DEFAULT_BACKOFF_MINUTES)))
            .webSocketFactory(okHttpClient.newWebSocketFactory(getServerUrl()))
            .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi))
            .addStreamAdapterFactory(FlowStreamAdapter.Factory())
            .build()
    }
    private val relay: RelayService by lazy { scarlet.create() }
    //endregion

    val eventsStream = relay.observeEventsStream()
    val events = relay.observeEvents()
    val publishResponse = relay.observePublishResponse()
    val subscribeResponse = relay.observeSubscribeResponse()
    val subscriptionRequest = relay.observeSubscriptionRequest()
    val unsubscribeResponse = relay.observeUnsubscribeResponse()

    fun publish(topic: Topic, preSettlementPairingApproval: PreSettlementPairing.Approve) {
        val publishRequest = preSettlementPairingApproval.toRelayPublishRequest(2, topic, moshi)
        println(publishRequest.toString())
        relay.publishRequest(publishRequest)
    }

    private fun getServerUrl(): String {
        return (if (useTLs) "wss" else "ws") +
                "://$hostName" +
                if (port > 0) ":$port" else ""
    }

    companion object {
        private const val DEFAULT_BACKOFF_MINUTES = 5L

        fun initRemote(useTLs: Boolean = false, hostName: String, port: Int = 0) =
            WakuRelayRepository(useTLs, hostName, port)
    }
}

