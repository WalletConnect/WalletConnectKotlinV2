package com.walletconnect.walletconnectv2.network.data.repository

import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.core.model.vo.SubscriptionIdVO
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.scope.scope
import com.walletconnect.walletconnectv2.network.Relay
import com.walletconnect.walletconnectv2.network.data.service.RelayService
import com.walletconnect.walletconnectv2.network.model.RelayDTO
import com.walletconnect.walletconnectv2.network.model.toRelayAcknowledgment
import com.walletconnect.walletconnectv2.network.model.toRelayEvent
import com.walletconnect.walletconnectv2.network.model.toRelayRequest
import com.walletconnect.walletconnectv2.util.Logger
import com.walletconnect.walletconnectv2.util.generateId
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class RelayClient internal constructor(private val relay: RelayService) : Relay {

    override val eventsFlow: SharedFlow<WalletConnect.Model.Relay.Event> = relay
        .observeWebSocketEvent()
        .map { it.toRelayEvent() }
        .shareIn(scope, SharingStarted.Lazily, REPLAY)

    override val subscriptionRequest: Flow<WalletConnect.Model.Relay.Call.Subscription.Request> =
        relay.observeSubscriptionRequest()
            .map { it.toRelayRequest() }
            .onEach { relayRequest -> supervisorScope { publishSubscriptionAcknowledgement(relayRequest.id) } }

    override fun publish(topic: String, message: String, prompt: Boolean, onResult: (Result<WalletConnect.Model.Relay.Call.Publish.Acknowledgement>) -> Unit) {
        val request = RelayDTO.Publish.Request(generateId(), params = RelayDTO.Publish.Request.Params(
            TopicVO(topic), message, prompt = prompt))
        observePublishAcknowledgement { acknowledgement -> onResult(Result.success(acknowledgement)) }
        observePublishError { error -> onResult(Result.failure(error)) }
        relay.publishRequest(request)
    }

    override fun subscribe(topic: String, onResult: (Result<WalletConnect.Model.Relay.Call.Subscribe.Acknowledgement>) -> Unit) {
        val subscribeRequest = RelayDTO.Subscribe.Request(id = generateId(), params = RelayDTO.Subscribe.Request.Params(TopicVO(topic)))
        observeSubscribeAcknowledgement { acknowledgement -> onResult(Result.success(acknowledgement)) }
        observeSubscribeError { error -> onResult(Result.failure(error)) }
        relay.subscribeRequest(subscribeRequest)
    }

    override fun unsubscribe(
        topic: String,
        subscriptionId: String,
        onResult: (Result<WalletConnect.Model.Relay.Call.Unsubscribe.Acknowledgement>) -> Unit,
    ) {
        val unsubscribeRequest =
            RelayDTO.Unsubscribe.Request(id = generateId(), params = RelayDTO.Unsubscribe.Request.Params(TopicVO(topic), SubscriptionIdVO(subscriptionId)))
        observeUnSubscribeAcknowledgement { acknowledgement -> onResult(Result.success(acknowledgement)) }
        observeUnSubscribeError { error -> onResult(Result.failure(error)) }
        relay.unsubscribeRequest(unsubscribeRequest)
    }

    private fun publishSubscriptionAcknowledgement(id: Long) {
        val publishRequest = RelayDTO.Subscription.Acknowledgement(id = id, result = true)
        relay.publishSubscriptionAcknowledgement(publishRequest)
    }

    private fun observePublishAcknowledgement(onResult: (WalletConnect.Model.Relay.Call.Publish.Acknowledgement) -> Unit) {
        scope.launch {
            relay.observePublishAcknowledgement()
                .map { it.toRelayAcknowledgment() }
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

    private fun observeSubscribeAcknowledgement(onResult: (WalletConnect.Model.Relay.Call.Subscribe.Acknowledgement) -> Unit) {
        scope.launch {
            relay.observeSubscribeAcknowledgement()
                .map { it.toRelayAcknowledgment() }
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

    private fun observeUnSubscribeAcknowledgement(onSuccess: (WalletConnect.Model.Relay.Call.Unsubscribe.Acknowledgement) -> Unit) {
        scope.launch {
            relay.observeUnsubscribeAcknowledgement()
                .map { it.toRelayAcknowledgment() }
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

    private companion object {
        private const val REPLAY: Int = 1
    }
}