package org.walletconnect.walletconnectv2.relay

import android.app.Application
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.utils.getRawType
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import okhttp3.OkHttpClient
import org.json.JSONObject
import org.walletconnect.walletconnectv2.clientsync.pairing.after.PostSettlementPairing
import org.walletconnect.walletconnectv2.clientsync.pairing.before.PreSettlementPairing
import org.walletconnect.walletconnectv2.clientsync.session.before.PreSettlementSession
import org.walletconnect.walletconnectv2.common.*
import org.walletconnect.walletconnectv2.common.network.adapters.*
import org.walletconnect.walletconnectv2.relay.data.RelayService
import org.walletconnect.walletconnectv2.relay.data.model.Relay
import org.walletconnect.walletconnectv2.util.adapters.FlowStreamAdapter
import org.walletconnect.walletconnectv2.util.generateId
import java.util.concurrent.TimeUnit

class WakuRelayRepository internal constructor(
    private val useTLs: Boolean,
    private val hostName: String,
    private val apiKey: String,
    private val application: Application
) {
    //region Move to DI module
    private val okHttpClient = OkHttpClient.Builder()
        .writeTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
        .readTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
        .callTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
        .connectTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
        .pingInterval(5, TimeUnit.SECONDS)
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
            .lifecycle(AndroidLifecycle.ofApplicationForeground(application))
            .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi))
            .addStreamAdapterFactory(FlowStreamAdapter.Factory())
            .build()
    }
    private val relay: RelayService by lazy { scarlet.create(RelayService::class.java) }
    //endregion

    internal val eventsFlow = relay.eventsFlow()
    internal val publishAcknowledgement = relay.observePublishAcknowledgement()
    internal val subscribeAcknowledgement = relay.observeSubscribeAcknowledgement()
    internal val subscriptionRequest = relay.observeSubscriptionRequest()
    internal val unsubscribeAcknowledgement = relay.observeUnsubscribeAcknowledgement()

    fun publishPairingApproval(
        topic: Topic,
        preSettlementPairingApproval: PreSettlementPairing.Approve
    ) {
        val publishRequest =
            preSettlementPairingApproval.toRelayPublishRequest(generateId(), topic, moshi)
        relay.publishRequest(publishRequest)
    }

    fun publish(topic: Topic, encryptedJson: String) {
        val publishRequest =
            Relay.Publish.Request(
                id = generateId(),
                params = Relay.Publish.Request.Params(topic = topic, message = encryptedJson)
            )
        relay.publishRequest(publishRequest)
    }

    fun publishSubscriptionAcknowledgment(id: Long) {
        val publishRequest =
            Relay.Subscription.Acknowledgement(id = id, result = true)
        relay.publishSubscriptionAcknowledgment(publishRequest)
    }

    fun subscribe(topic: Topic) {
        val subscribeRequest =
            Relay.Subscribe.Request(
                id = generateId(),
                params = Relay.Subscribe.Request.Params(topic)
            )
        relay.subscribeRequest(subscribeRequest)
    }

    fun getSessionApprovalJson(preSettlementSessionApproval: PreSettlementSession.Approve): String =
        moshi.adapter(PreSettlementSession.Approve::class.java).toJson(preSettlementSessionApproval)

    fun getSessionRejectionJson(preSettlementSessionRejection: PreSettlementSession.Reject): String =
        moshi.adapter(PreSettlementSession.Reject::class.java).toJson(preSettlementSessionRejection)

    fun parseToPairingPayload(json: String): PostSettlementPairing.PairingPayload? =
        moshi.adapter(PostSettlementPairing.PairingPayload::class.java).fromJson(json)

    private fun getServerUrl(): String {
        return ((if (useTLs) "wss" else "ws") + "://$hostName/?apiKey=$apiKey").trim()
    }

    companion object {
        private const val TIMEOUT_TIME = 5000L
        private const val DEFAULT_BACKOFF_MINUTES = 5L

        fun initRemote(
            useTLs: Boolean = false,
            hostName: String,
            apiKey: String,
            application: Application
        ) = WakuRelayRepository(useTLs, hostName, apiKey, application)
    }
}