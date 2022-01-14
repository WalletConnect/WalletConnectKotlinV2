package com.walletconnect.walletconnectv2.network.data.repository

import android.app.Application
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import okhttp3.OkHttpClient
import com.walletconnect.walletconnectv2.common.model.vo.SubscriptionIdVO
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.common.scope.moshi
import com.walletconnect.walletconnectv2.common.scope.scope
import com.walletconnect.walletconnectv2.network.NetworkRepository
import com.walletconnect.walletconnectv2.network.data.adapter.FlowStreamAdapter
import com.walletconnect.walletconnectv2.network.data.service.RelayService
import com.walletconnect.walletconnectv2.network.model.RelayDTO
import com.walletconnect.walletconnectv2.util.Logger
import com.walletconnect.walletconnectv2.util.generateId
import java.util.concurrent.TimeUnit

class WakuNetworkRepository internal constructor(
    private val useTLs: Boolean,
    private val hostName: String,
    private val projectId: String,
    private val application: Application
) : NetworkRepository {
    //region Move to DI module
    private val okHttpClient = OkHttpClient.Builder()
        .writeTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
        .readTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
        .callTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
        .connectTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
        .build()

    private val scarlet by lazy {
        Scarlet.Builder()
            .backoffStrategy(LinearBackoffStrategy(TimeUnit.MINUTES.toMillis(DEFAULT_BACKOFF_MINUTES)))
            .webSocketFactory(okHttpClient.newWebSocketFactory(getServerUrl()))
            .lifecycle(AndroidLifecycle.ofApplicationForeground(application)) // TODO: Maybe have debug version of scarlet w/o application and release version of scarlet w/ application once DI is setup
            .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi))
            .addStreamAdapterFactory(FlowStreamAdapter.Factory())
            .build()
    }
    private val relay: RelayService by lazy { scarlet.create(RelayService::class.java) }
    //endregion

    internal val eventsFlow: SharedFlow<WebSocket.Event> = relay.eventsFlow().shareIn(scope, SharingStarted.Lazily, REPLAY)
    internal val observePublishAcknowledgement: Flow<RelayDTO.Publish.Acknowledgement> = relay.observePublishAcknowledgement()

    internal val subscriptionRequest: Flow<RelayDTO.Subscription.Request> =
        relay.observeSubscriptionRequest()
            .onEach { relayRequest -> supervisorScope { publishSubscriptionAcknowledgement(relayRequest.id) } }

   internal fun publish(topic: TopicVO, message: String, onResult: (Result<RelayDTO.Publish.Acknowledgement>) -> Unit = {}) {
        val publishRequest =
            RelayDTO.Publish.Request(id = generateId(), params = RelayDTO.Publish.Request.Params(topic = topic, message = message))
        observePublishAcknowledgement { acknowledgement -> onResult(Result.success(acknowledgement)) }
        observePublishError { error -> onResult(Result.failure(error)) }
        relay.publishRequest(publishRequest)
    }

   internal fun subscribe(topic: TopicVO, onResult: (Result<RelayDTO.Subscribe.Acknowledgement>) -> Unit) {
        val subscribeRequest = RelayDTO.Subscribe.Request(id = generateId(), params = RelayDTO.Subscribe.Request.Params(topic))
        observeSubscribeAcknowledgement { acknowledgement -> onResult(Result.success(acknowledgement)) }
        observeSubscribeError { error -> onResult(Result.failure(error)) }
        relay.subscribeRequest(subscribeRequest)
    }

   internal fun unsubscribe(topic: TopicVO, subscriptionId: SubscriptionIdVO, onResult: (Result<RelayDTO.Unsubscribe.Acknowledgement>) -> Unit) {
        val unsubscribeRequest =
            RelayDTO.Unsubscribe.Request(id = generateId(), params = RelayDTO.Unsubscribe.Request.Params(topic, subscriptionId))
        observeUnSubscribeAcknowledgement { acknowledgement -> onResult(Result.success(acknowledgement)) }
        observeUnSubscribeError { error -> onResult(Result.failure(error)) }
        relay.unsubscribeRequest(unsubscribeRequest)
    }

    private fun publishSubscriptionAcknowledgement(id: Long) {
        val publishRequest = RelayDTO.Subscription.Acknowledgement(id = id, result = true)
        relay.publishSubscriptionAcknowledgement(publishRequest)
    }

    private fun observePublishAcknowledgement(onResult: (RelayDTO.Publish.Acknowledgement) -> Unit) {
        scope.launch {
            relay.observePublishAcknowledgement()
                .catch { exception -> Logger.error(exception) }
                .collect { acknowledgement ->
                    supervisorScope {
                        onResult(acknowledgement)
                        cancel()
                    }
                }
        }
    }

    private fun observePublishError(onFailure: (Throwable) -> Unit) {
        scope.launch {
            relay.observePublishError()
                .onEach { jsonRpcError -> Logger.error(Throwable(jsonRpcError.error.errorMessage)) }
                .catch { exception -> Logger.error(exception) }
                .collect { errorResponse ->
                    supervisorScope {
                        onFailure(Throwable(errorResponse.error.errorMessage))
                        cancel()
                    }
                }
        }
    }

    private fun observeSubscribeAcknowledgement(onResult: (RelayDTO.Subscribe.Acknowledgement) -> Unit) {
        scope.launch {
            relay.observeSubscribeAcknowledgement()
                .catch { exception -> Logger.error(exception) }
                .collect { acknowledgement ->
                    supervisorScope {
                        onResult(acknowledgement)
                        cancel()
                    }
                }
        }
    }

    private fun observeSubscribeError(onFailure: (Throwable) -> Unit) {
        scope.launch {
            relay.observeSubscribeError()
                .onEach { jsonRpcError -> Logger.error(Throwable(jsonRpcError.error.errorMessage)) }
                .catch { exception -> Logger.error(exception) }
                .collect { errorResponse ->
                    supervisorScope {
                        onFailure(Throwable(errorResponse.error.errorMessage))
                        cancel()
                    }
                }
        }
    }

    private fun observeUnSubscribeAcknowledgement(onSuccess: (RelayDTO.Unsubscribe.Acknowledgement) -> Unit) {
        scope.launch {
            relay.observeUnsubscribeAcknowledgement()
                .catch { exception -> Logger.error(exception) }
                .collect { acknowledgement ->
                    supervisorScope {
                        onSuccess(acknowledgement)
                        cancel()
                    }
                }
        }
    }

    private fun observeUnSubscribeError(onFailure: (Throwable) -> Unit) {
        scope.launch {
            relay.observeUnsubscribeError()
                .onEach { jsonRpcError -> Logger.error(Throwable(jsonRpcError.error.errorMessage)) }
                .catch { exception -> Logger.error(exception) }
                .collect { errorResponse ->
                    supervisorScope {
                        onFailure(Throwable(errorResponse.error.errorMessage))
                        cancel()
                    }
                }
        }
    }

    private fun getServerUrl(): String =
        ((if (useTLs) "wss" else "ws") + "://$hostName/?projectId=$projectId").trim()

    class WakuNetworkFactory(
        val useTls: Boolean,
        val hostName: String,
        val projectId: String,
        val application: Application
    )

    companion object {
        private const val TIMEOUT_TIME: Long = 5000L
        private const val DEFAULT_BACKOFF_MINUTES: Long = 5L
        private const val REPLAY: Int = 1

        fun init(wakuNetworkFactory: WakuNetworkFactory) = with(wakuNetworkFactory) {
            WakuNetworkRepository(useTls, hostName, projectId, application)
        }
    }
}