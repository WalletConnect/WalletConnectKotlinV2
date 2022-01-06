package com.walletconnect.walletconnectv2.relay.waku

import com.tinder.scarlet.WebSocket
import com.walletconnect.walletconnectv2.common.SubscriptionId
import com.walletconnect.walletconnectv2.common.Topic
import com.walletconnect.walletconnectv2.scope
import com.walletconnect.walletconnectv2.util.Logger
import com.walletconnect.walletconnectv2.util.generateId
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WakuNetworkRepository @Inject constructor(private val relayService: RelayService) {
    internal val eventsFlow: SharedFlow<WebSocket.Event> = relayService.eventsFlow().shareIn(scope, SharingStarted.Lazily, REPLAY)
    internal val observePublishAcknowledgement: Flow<Relay.Publish.Acknowledgement> = relayService.observePublishAcknowledgement()

    internal val subscriptionRequest: Flow<Relay.Subscription.Request> =
        relayService.observeSubscriptionRequest()
            .onEach { relayRequest -> supervisorScope { publishSubscriptionAcknowledgement(relayRequest.id) } }

    fun publish(topic: Topic, message: String, onResult: (Result<Relay.Publish.Acknowledgement>) -> Unit = {}) {
        val publishRequest =
            Relay.Publish.Request(id = generateId(), params = Relay.Publish.Request.Params(topic = topic, message = message))
        observePublishAcknowledgement { acknowledgement -> onResult(Result.success(acknowledgement)) }
        observePublishError { error -> onResult(Result.failure(error)) }
        relayService.publishRequest(publishRequest)
    }

    fun subscribe(topic: Topic, onResult: (Result<Relay.Subscribe.Acknowledgement>) -> Unit) {
        val subscribeRequest = Relay.Subscribe.Request(id = generateId(), params = Relay.Subscribe.Request.Params(topic))
        observeSubscribeAcknowledgement { acknowledgement -> onResult(Result.success(acknowledgement)) }
        observeSubscribeError { error -> onResult(Result.failure(error)) }
        relayService.subscribeRequest(subscribeRequest)
    }

    fun unsubscribe(topic: Topic, subscriptionId: SubscriptionId, onResult: (Result<Relay.Unsubscribe.Acknowledgement>) -> Unit) {
        val unsubscribeRequest =
            Relay.Unsubscribe.Request(id = generateId(), params = Relay.Unsubscribe.Request.Params(topic, subscriptionId))
        observeUnSubscribeAcknowledgement { acknowledgement -> onResult(Result.success(acknowledgement)) }
        observeUnSubscribeError { error -> onResult(Result.failure(error)) }
        relayService.unsubscribeRequest(unsubscribeRequest)
    }

    private fun publishSubscriptionAcknowledgement(id: Long) {
        val publishRequest = Relay.Subscription.Acknowledgement(id = id, result = true)
        relayService.publishSubscriptionAcknowledgement(publishRequest)
    }

    private fun observePublishAcknowledgement(onResult: (Relay.Publish.Acknowledgement) -> Unit) {
        scope.launch {
            relayService.observePublishAcknowledgement()
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
            relayService.observePublishError()
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

    private fun observeSubscribeAcknowledgement(onResult: (Relay.Subscribe.Acknowledgement) -> Unit) {
        scope.launch {
            relayService.observeSubscribeAcknowledgement()
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
            relayService.observeSubscribeError()
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

    private fun observeUnSubscribeAcknowledgement(onSuccess: (Relay.Unsubscribe.Acknowledgement) -> Unit) {
        scope.launch {
            relayService.observeUnsubscribeAcknowledgement()
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
            relayService.observeUnsubscribeError()
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

    companion object {
        private const val REPLAY: Int = 1
    }
}