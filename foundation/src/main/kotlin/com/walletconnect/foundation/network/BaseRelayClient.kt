package com.walletconnect.foundation.network

import com.walletconnect.foundation.common.model.SubscriptionId
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.common.toRelay
import com.walletconnect.foundation.common.toRelayEvent
import com.walletconnect.foundation.di.commonModule
import com.walletconnect.foundation.network.data.service.RelayService
import com.walletconnect.foundation.network.model.Relay
import com.walletconnect.foundation.network.model.RelayDTO
import com.walletconnect.foundation.util.Logger
import com.walletconnect.foundation.util.scope
import com.walletconnect.util.generateId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.koin.core.KoinApplication

abstract class BaseRelayClient : RelayInterface {
    private var foundationKoinApp: KoinApplication = KoinApplication.init()
    lateinit var relayService: RelayService
    protected open lateinit var logger: Logger

    init {
        foundationKoinApp.run { modules(commonModule()) }
        logger = foundationKoinApp.koin.get()
    }

    override val eventsFlow: SharedFlow<Relay.Model.Event> by lazy {
        relayService
            .observeWebSocketEvent()
            .map { event -> event.toRelayEvent() }
            .shareIn(scope, SharingStarted.Lazily, REPLAY)
    }

    override val subscriptionRequest: Flow<Relay.Model.Call.Subscription.Request> by lazy {
        relayService.observeSubscriptionRequest()
            .map { request -> request.toRelay() }
            .onEach { relayRequest -> supervisorScope { publishSubscriptionAcknowledgement(relayRequest.id) } }
    }

    @ExperimentalCoroutinesApi
    override fun publish(
        topic: String,
        message: String,
        params: Relay.Model.IrnParams,
        onResult: (Result<Relay.Model.Call.Publish.Acknowledgement>) -> Unit,
    ) {
        val (tag, ttl, prompt) = params
        val publishParams = RelayDTO.Publish.Request.Params(Topic(topic), message, Ttl(ttl), tag, prompt)
        val request = RelayDTO.Publish.Request(generateId(), params = publishParams)

        observePublishResult(onResult)
        relayService.publishRequest(request)
    }

    @ExperimentalCoroutinesApi
    private fun observePublishResult(onResult: (Result<Relay.Model.Call.Publish.Acknowledgement>) -> Unit) {
        scope.launch {
            merge(relayService.observePublishAcknowledgement(), relayService.observePublishError())
                .catch { exception -> logger.error(exception) }
                .collect { publishResult ->
                    supervisorScope {
                        when (publishResult) {
                            is RelayDTO.Publish.Result.Acknowledgement -> onResult(Result.success(publishResult.toRelay()))
                            is RelayDTO.Publish.Result.JsonRpcError -> onResult(Result.failure(Throwable(publishResult.error.errorMessage)))
                        }
                        cancel()
                    }
                }
        }
    }

    @ExperimentalCoroutinesApi
    override fun subscribe(topic: String, onResult: (Result<Relay.Model.Call.Subscribe.Acknowledgement>) -> Unit) {
        val subscribeRequest = RelayDTO.Subscribe.Request(id = generateId(), params = RelayDTO.Subscribe.Request.Params(Topic(topic)))

        observeSubscribeResult(onResult)
        relayService.subscribeRequest(subscribeRequest)
    }

    @ExperimentalCoroutinesApi
    private fun observeSubscribeResult(onResult: (Result<Relay.Model.Call.Subscribe.Acknowledgement>) -> Unit) {
        scope.launch {
            merge(relayService.observeSubscribeAcknowledgement(), relayService.observeSubscribeError())
                .catch { exception -> logger.error(exception) }
                .collect { subscribeResult ->
                    supervisorScope {
                        when (subscribeResult) {
                            is RelayDTO.Subscribe.Result.Acknowledgement -> onResult(Result.success(subscribeResult.toRelay()))
                            is RelayDTO.Subscribe.Result.JsonRpcError -> onResult(Result.failure(Throwable(subscribeResult.error.errorMessage)))
                        }
                        cancel()
                    }
                }
        }
    }

    @ExperimentalCoroutinesApi
    override fun unsubscribe(
        topic: String,
        subscriptionId: String,
        onResult: (Result<Relay.Model.Call.Unsubscribe.Acknowledgement>) -> Unit,
    ) {
        val unsubscribeRequest = RelayDTO.Unsubscribe.Request(
            id = generateId(),
            params = RelayDTO.Unsubscribe.Request.Params(Topic(topic), SubscriptionId(subscriptionId))
        )

        observeUnsubscribeResult(onResult)
        relayService.unsubscribeRequest(unsubscribeRequest)
    }

    @ExperimentalCoroutinesApi
    private fun observeUnsubscribeResult(onResult: (Result<Relay.Model.Call.Unsubscribe.Acknowledgement>) -> Unit) {
        scope.launch {
            merge(relayService.observeUnsubscribeAcknowledgement(), relayService.observeUnsubscribeError())
                .catch { exception -> logger.error(exception) }
                .collect { subscribeResult ->
                    supervisorScope {
                        when (subscribeResult) {
                            is RelayDTO.Unsubscribe.Result.Acknowledgement -> onResult(Result.success(subscribeResult.toRelay()))
                            is RelayDTO.Unsubscribe.Result.JsonRpcError -> onResult(Result.failure(Throwable(subscribeResult.error.errorMessage)))
                        }
                        cancel()
                    }
                }
        }
    }

    private fun publishSubscriptionAcknowledgement(id: Long) {
        val publishRequest = RelayDTO.Subscription.Result.Acknowledgement(id = id, result = true)
        relayService.publishSubscriptionAcknowledgement(publishRequest)
    }

    private companion object {
        const val REPLAY: Int = 1
    }
}