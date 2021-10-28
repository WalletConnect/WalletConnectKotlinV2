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
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import org.walletconnect.walletconnectv2.clientcomm.PreSettlementPairing
import org.walletconnect.walletconnectv2.clientcomm.PreSettlementSession
import org.walletconnect.walletconnectv2.clientcomm.pairing.PairingPayload
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
    private val port: Int,
    private val application: Application
) {
    //region Move to DI module
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .writeTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
        .readTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
        .callTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
        .connectTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
        .pingInterval(2, TimeUnit.SECONDS)
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

    internal val eventsStream = relay.observeEvents()
    internal val publishAcknowledgement: Flow<Relay.Publish.Acknowledgement> =
        relay.observePublishAcknowledgement()
    internal val subscribeAcknowledgement: Flow<Relay.Subscribe.Acknowledgement> =
        relay.observeSubscribeAcknowledgement()
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

    fun getSessionApprovalJson(preSettlementSessionApproval: PreSettlementSession.Approve): String {
        return moshi.adapter(PreSettlementSession.Approve::class.java)
            .toJson(preSettlementSessionApproval)
    }

    fun publishSessionApproval(
        topic: Topic,
        encryptedJson: String
    ) {
        val publishRequest =
            Relay.Publish.Request(
                id = generateId(),
                params = Relay.Publish.Request.Params(topic = topic, message = encryptedJson)
            )
        relay.publishRequest(publishRequest)
    }

    fun publishSessionProposalAcknowledgment(id: Long) {
        val publishRequest =
            Relay.Subscription.Acknowledgement(
                id = id,
                result = true
            )
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

    fun parseToPairingPayload(json: String): PairingPayload? =
        moshi.adapter(PairingPayload::class.java).fromJson(json)

    private fun getServerUrl(): String {
        return (if (useTLs) "wss" else "ws") + "://$hostName" + if (port > 0) ":$port" else ""
    }

    companion object {
        private const val TIMEOUT_TIME = 5000L
        private const val DEFAULT_BACKOFF_MINUTES = 5L

        fun initRemote(
            useTLs: Boolean = false,
            hostName: String,
            port: Int = 0,
            application: Application
        ) = WakuRelayRepository(useTLs, hostName, port, application)
    }
}