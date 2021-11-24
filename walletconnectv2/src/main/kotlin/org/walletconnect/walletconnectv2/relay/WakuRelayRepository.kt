package org.walletconnect.walletconnectv2.relay

import android.app.Application
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.supervisorScope
import okhttp3.OkHttpClient
import org.walletconnect.walletconnectv2.common.SubscriptionId
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.moshi
import org.walletconnect.walletconnectv2.relay.data.RelayService
import org.walletconnect.walletconnectv2.relay.data.model.Relay
import org.walletconnect.walletconnectv2.scope
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

    private val scarlet by lazy {
        Scarlet.Builder()
            .backoffStrategy(LinearBackoffStrategy(TimeUnit.MINUTES.toMillis(DEFAULT_BACKOFF_MINUTES)))
            .webSocketFactory(okHttpClient.newWebSocketFactory(getServerUrl()))
//            .lifecycle(AndroidLifecycle.ofApplicationForeground(application)) // TODO: Maybe have debug version of scarlet w/o application and release version of scarlet w/ application once DI is setup
            .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi))
            .addStreamAdapterFactory(FlowStreamAdapter.Factory())
            .build()
    }
    private val relay: RelayService by lazy { scarlet.create(RelayService::class.java) }
    //endregion

    internal val eventsFlow: SharedFlow<WebSocket.Event> = relay.eventsFlow().shareIn(scope, SharingStarted.Lazily, REPLAY)
    internal val observePublishAcknowledgement: Flow<Relay.Publish.Acknowledgement> = relay.observePublishAcknowledgement()
    internal val observePublishResponseError: Flow<Relay.Publish.JsonRpcError> = relay.observePublishError()
    internal val observeSubscribeResponse = relay.observeSubscribeAcknowledgement()
    internal val observeUnsubscribeResponse = relay.observeUnsubscribeAcknowledgement()
    internal val subscriptionRequest: Flow<Relay.Subscription.Request> =
        relay.observeSubscriptionRequest()
            .onEach { relayRequest -> supervisorScope { publishSubscriptionAcknowledgement(relayRequest.id) } }

    fun publish(topic: Topic, message: String) {
        val publishRequest =
            Relay.Publish.Request(id = generateId(), params = Relay.Publish.Request.Params(topic = topic, message = message))
        relay.publishRequest(publishRequest)
    }

    fun subscribe(topic: Topic) {
        val subscribeRequest = Relay.Subscribe.Request(id = generateId(), params = Relay.Subscribe.Request.Params(topic))
        relay.subscribeRequest(subscribeRequest)
    }

    fun unsubscribe(topic: Topic, subscriptionId: SubscriptionId) {
        val unsubscribeRequest =
            Relay.Unsubscribe.Request(id = generateId(), params = Relay.Unsubscribe.Request.Params(topic, subscriptionId))
        relay.unsubscribeRequest(unsubscribeRequest)
    }

    private fun publishSubscriptionAcknowledgement(id: Long) {
        val publishRequest = Relay.Subscription.Acknowledgement(id = id, result = true)
        relay.publishSubscriptionAcknowledgement(publishRequest)
    }

    private fun getServerUrl(): String =
        ((if (useTLs) "wss" else "ws") + "://$hostName/?apiKey=$apiKey").trim()

    class RelayFactory(
        val useTls: Boolean,
        val hostName: String,
        val apiKey: String,
        val application: Application
    )

    companion object {
        private const val TIMEOUT_TIME: Long = 5000L
        private const val DEFAULT_BACKOFF_MINUTES: Long = 5L
        private const val REPLAY: Int = 1

        fun initRemote(relayFactory: RelayFactory) = with(relayFactory) {
            WakuRelayRepository(useTls, hostName, apiKey, application)
        }
    }
}