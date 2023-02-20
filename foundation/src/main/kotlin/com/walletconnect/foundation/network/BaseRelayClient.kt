package com.walletconnect.foundation.network

import com.walletconnect.foundation.common.model.SubscriptionId
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.common.toRelay
import com.walletconnect.foundation.common.toRelayEvent
import com.walletconnect.foundation.di.foundationCommonModule
import com.walletconnect.foundation.network.data.service.RelayService
import com.walletconnect.foundation.network.model.Relay
import com.walletconnect.foundation.network.model.RelayDTO
import com.walletconnect.foundation.util.Logger
import com.walletconnect.foundation.util.scope
import com.walletconnect.util.generateId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.koin.core.KoinApplication

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseRelayClient : RelayInterface {
    private var foundationKoinApp: KoinApplication = KoinApplication.init()
    lateinit var relayService: RelayService
    protected var logger: Logger
    private val resultState: MutableSharedFlow<RelayDTO> = MutableSharedFlow()

    init {
        foundationKoinApp.run { modules(foundationCommonModule()) }
        logger = foundationKoinApp.koin.get()
    }

    fun observeResults() {
        scope.launch {
            merge(
                relayService.observePublishAcknowledgement(),
                relayService.observePublishError(),
                relayService.observeBatchSubscribeAcknowledgement(),
                relayService.observeBatchSubscribeError(),
                relayService.observeSubscribeAcknowledgement(),
                relayService.observeSubscribeError(),
                relayService.observeUnsubscribeAcknowledgement(),
                relayService.observeUnsubscribeError()
            )
                .catch { exception -> logger.error(exception) }
                .collect { result -> resultState.emit(result) }
        }
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
        val publishRequest = RelayDTO.Publish.Request(generateId(), params = publishParams)

        observePublishResult(publishRequest.id, onResult)
        relayService.publishRequest(publishRequest)
    }

    private fun observePublishResult(id: Long, onResult: (Result<Relay.Model.Call.Publish.Acknowledgement>) -> Unit) {
        resultState
            .filterIsInstance<RelayDTO.Publish.Result>()
            .filter { relayResult -> relayResult.id == id }
            .onEach { publishResult ->
                when (publishResult) {
                    is RelayDTO.Publish.Result.Acknowledgement -> onResult(Result.success(publishResult.toRelay()))
                    is RelayDTO.Publish.Result.JsonRpcError -> onResult(Result.failure(Throwable(publishResult.error.errorMessage)))
                }
            }.launchIn(scope)
    }

    @ExperimentalCoroutinesApi
    override fun subscribe(topic: String, onResult: (Result<Relay.Model.Call.Subscribe.Acknowledgement>) -> Unit) {
        val subscribeRequest = RelayDTO.Subscribe.Request(id = generateId(), params = RelayDTO.Subscribe.Request.Params(Topic(topic)))

        observeSubscribeResult(subscribeRequest.id, onResult)
        relayService.subscribeRequest(subscribeRequest)
    }

    private fun observeSubscribeResult(id: Long, onResult: (Result<Relay.Model.Call.Subscribe.Acknowledgement>) -> Unit) {
        resultState
            .filterIsInstance<RelayDTO.Subscribe.Result>()
            .filter { relayResult -> relayResult.id == id }
            .onEach { subscribeResult ->
                when (subscribeResult) {
                    is RelayDTO.Subscribe.Result.Acknowledgement -> onResult(Result.success(subscribeResult.toRelay()))
                    is RelayDTO.Subscribe.Result.JsonRpcError -> onResult(Result.failure(Throwable(subscribeResult.error.errorMessage)))
                }
            }.launchIn(scope)
    }

    @ExperimentalCoroutinesApi
    override fun batchSubscribe(topics: List<String>, onResult: (Result<Relay.Model.Call.BatchSubscribe.Acknowledgement>) -> Unit) {
        val batchSubscribeRequest = RelayDTO.BatchSubscribe.Request(id = generateId(), params = RelayDTO.BatchSubscribe.Request.Params(topics))


        observeBatchSubscribeResult(batchSubscribeRequest.id, onResult)
        relayService.batchSubscribeRequest(batchSubscribeRequest)
    }

    private fun observeBatchSubscribeResult(id: Long, onResult: (Result<Relay.Model.Call.BatchSubscribe.Acknowledgement>) -> Unit) {
        resultState
            .filterIsInstance<RelayDTO.BatchSubscribe.Result>()
            .filter { relayResult -> relayResult.id == id }
            .onEach { batchSubscribeResult ->
                when (batchSubscribeResult) {
                    is RelayDTO.BatchSubscribe.Result.Acknowledgement -> onResult(Result.success(batchSubscribeResult.toRelay()))
                    is RelayDTO.BatchSubscribe.Result.JsonRpcError -> onResult(Result.failure(Throwable(batchSubscribeResult.error.errorMessage)))
                }
            }.launchIn(scope)
    }

    @ExperimentalCoroutinesApi
    override fun unsubscribe(
        topic: String,
        subscriptionId: String,
        onResult: (Result<Relay.Model.Call.Unsubscribe.Acknowledgement>) -> Unit,
    ) {
        val unsubscribeRequest = RelayDTO.Unsubscribe.Request(id = generateId(), params = RelayDTO.Unsubscribe.Request.Params(Topic(topic), SubscriptionId(subscriptionId)))

        observeUnsubscribeResult(unsubscribeRequest.id, onResult)
        relayService.unsubscribeRequest(unsubscribeRequest)
    }

    private fun observeUnsubscribeResult(id: Long, onResult: (Result<Relay.Model.Call.Unsubscribe.Acknowledgement>) -> Unit) {
        resultState
            .filterIsInstance<RelayDTO.Unsubscribe.Result>()
            .filter { relayResult -> relayResult.id == id }
            .onEach { unsubscribeResult ->
                when (unsubscribeResult) {
                    is RelayDTO.Unsubscribe.Result.Acknowledgement -> onResult(Result.success(unsubscribeResult.toRelay()))
                    is RelayDTO.Unsubscribe.Result.JsonRpcError -> onResult(Result.failure(Throwable(unsubscribeResult.error.errorMessage)))
                }
            }.launchIn(scope)
    }

    private fun publishSubscriptionAcknowledgement(id: Long) {
        val publishRequest = RelayDTO.Subscription.Result.Acknowledgement(id = id, result = true)
        relayService.publishSubscriptionAcknowledgement(publishRequest)
    }

    private companion object {
        const val REPLAY: Int = 1
    }
}