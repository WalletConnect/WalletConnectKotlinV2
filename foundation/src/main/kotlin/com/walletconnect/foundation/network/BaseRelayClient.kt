package com.walletconnect.foundation.network

import com.tinder.scarlet.WebSocket
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
import com.walletconnect.util.generateClientToServerId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import org.koin.core.KoinApplication

sealed class ConnectionState {
    data object Open : ConnectionState()
    data class Closed(val throwable: Throwable) : ConnectionState()
    data object Idle : ConnectionState()
}

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseRelayClient : RelayInterface {
    private var foundationKoinApp: KoinApplication = KoinApplication.init()
    lateinit var relayService: RelayService
    lateinit var connectionLifecycle: ConnectionLifecycle
    protected var logger: Logger
    private val resultState: MutableSharedFlow<RelayDTO> = MutableSharedFlow()
    internal var connectionState: MutableStateFlow<ConnectionState> = MutableStateFlow(ConnectionState.Idle)
    private var unAckedTopics: MutableList<String> = mutableListOf()
    private var isConnecting: Boolean = false
    private var retryCount: Int = 0
    override var isLoggingEnabled: Boolean = false

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
                .collect { result ->
                    if (isLoggingEnabled) {
                        println("Result: $result; timestamp: ${System.currentTimeMillis()}")
                    }

                    resultState.emit(result)
                }
        }
    }

    override val eventsFlow: SharedFlow<Relay.Model.Event> by lazy {
        relayService
            .observeWebSocketEvent()
            .onEach { event ->
                if (event is WebSocket.Event.OnConnectionOpened<*>) {
                    connectionState.value = ConnectionState.Open
                } else if (event is WebSocket.Event.OnConnectionClosed || event is WebSocket.Event.OnConnectionFailed) {
                    connectionState.value = ConnectionState.Closed(getError(event))
                }
            }
            .map { event ->
                logger.log("Event: $event")
                event.toRelayEvent()
            }
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
        id: Long?,
        onResult: (Result<Relay.Model.Call.Publish.Acknowledgement>) -> Unit,
    ) {
        connectAndCallRelay(
            onConnected = {
                val (tag, ttl, prompt) = params
                val publishParams = RelayDTO.Publish.Request.Params(Topic(topic), message, Ttl(ttl), tag, prompt)
                val publishRequest = RelayDTO.Publish.Request(id = id ?: generateClientToServerId(), params = publishParams)
                observePublishResult(publishRequest.id, onResult)
                relayService.publishRequest(publishRequest)
            },
            onFailure = { onResult(Result.failure(it)) }
        )
    }

    private fun observePublishResult(id: Long, onResult: (Result<Relay.Model.Call.Publish.Acknowledgement>) -> Unit) {
        scope.launch {
            try {
                withTimeout(RESULT_TIMEOUT) {
                    resultState
                        .filterIsInstance<RelayDTO.Publish.Result>()
                        .filter { relayResult -> relayResult.id == id }
                        .first { publishResult ->
                            when (publishResult) {
                                is RelayDTO.Publish.Result.Acknowledgement -> onResult(Result.success(publishResult.toRelay()))
                                is RelayDTO.Publish.Result.JsonRpcError -> onResult(Result.failure(Throwable(publishResult.error.errorMessage)))
                            }
                            true
                        }
                }
            } catch (e: TimeoutCancellationException) {
                onResult(Result.failure(e))
                cancelJobIfActive()
            } catch (e: Exception) {
                onResult(Result.failure(e))
                cancelJobIfActive()
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun subscribe(topic: String, id: Long?, onResult: (Result<Relay.Model.Call.Subscribe.Acknowledgement>) -> Unit) {
        connectAndCallRelay(
            onConnected = {
                val subscribeRequest = RelayDTO.Subscribe.Request(id = id ?: generateClientToServerId(), params = RelayDTO.Subscribe.Request.Params(Topic(topic)))
                if (isLoggingEnabled) {
                    logger.log("Sending SubscribeRequest: $subscribeRequest;  timestamp: ${System.currentTimeMillis()}")
                }
                observeSubscribeResult(subscribeRequest.id, onResult)
                relayService.subscribeRequest(subscribeRequest)
            },
            onFailure = { onResult(Result.failure(it)) }
        )
    }

    private fun observeSubscribeResult(id: Long, onResult: (Result<Relay.Model.Call.Subscribe.Acknowledgement>) -> Unit) {
        scope.launch {
            try {
                withTimeout(RESULT_TIMEOUT) {
                    if (isLoggingEnabled) println("ObserveSubscribeResult: $id; timestamp: ${System.currentTimeMillis()}")
                    resultState
                        .onEach { relayResult -> if (isLoggingEnabled) logger.log("SubscribeResult 1: $relayResult") }
                        .filterIsInstance<RelayDTO.Subscribe.Result>()
                        .onEach { relayResult -> if (isLoggingEnabled) logger.log("SubscribeResult 2: $relayResult") }
                        .filter { relayResult -> relayResult.id == id }
                        .first { subscribeResult ->
                            if (isLoggingEnabled) println("SubscribeResult 3: $subscribeResult")
                            when (subscribeResult) {
                                is RelayDTO.Subscribe.Result.Acknowledgement -> onResult(Result.success(subscribeResult.toRelay()))
                                is RelayDTO.Subscribe.Result.JsonRpcError -> onResult(Result.failure(Throwable(subscribeResult.error.errorMessage)))
                            }
                            true
                        }
                }
            } catch (e: TimeoutCancellationException) {
                onResult(Result.failure(e))
                cancelJobIfActive()
            } catch (e: Exception) {
                onResult(Result.failure(e))
                cancelJobIfActive()
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun batchSubscribe(topics: List<String>, id: Long?, onResult: (Result<Relay.Model.Call.BatchSubscribe.Acknowledgement>) -> Unit) {
        connectAndCallRelay(
            onConnected = {
                if (!unAckedTopics.containsAll(topics)) {
                    unAckedTopics.addAll(topics)
                    val batchSubscribeRequest = RelayDTO.BatchSubscribe.Request(id = id ?: generateClientToServerId(), params = RelayDTO.BatchSubscribe.Request.Params(topics))
                    observeBatchSubscribeResult(batchSubscribeRequest.id, topics, onResult)
                    relayService.batchSubscribeRequest(batchSubscribeRequest)
                }
            },
            onFailure = { onResult(Result.failure(it)) }
        )
    }

    private fun observeBatchSubscribeResult(id: Long, topics: List<String>, onResult: (Result<Relay.Model.Call.BatchSubscribe.Acknowledgement>) -> Unit) {
        scope.launch {
            try {
                withTimeout(RESULT_TIMEOUT) {
                    resultState
                        .filterIsInstance<RelayDTO.BatchSubscribe.Result>()
                        .onEach { if (unAckedTopics.isNotEmpty()) unAckedTopics.removeAll(topics) }
                        .filter { relayResult -> relayResult.id == id }
                        .first { batchSubscribeResult ->
                            when (batchSubscribeResult) {
                                is RelayDTO.BatchSubscribe.Result.Acknowledgement -> onResult(Result.success(batchSubscribeResult.toRelay()))
                                is RelayDTO.BatchSubscribe.Result.JsonRpcError -> onResult(Result.failure(Throwable(batchSubscribeResult.error.errorMessage)))
                            }
                            true
                        }
                }
            } catch (e: TimeoutCancellationException) {
                onResult(Result.failure(e))
                cancelJobIfActive()
            } catch (e: Exception) {
                onResult(Result.failure(e))
                cancelJobIfActive()
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun unsubscribe(
        topic: String,
        subscriptionId: String,
        id: Long?,
        onResult: (Result<Relay.Model.Call.Unsubscribe.Acknowledgement>) -> Unit,
    ) {
        connectAndCallRelay(
            onConnected = {
                val unsubscribeRequest = RelayDTO.Unsubscribe.Request(
                    id = id ?: generateClientToServerId(),
                    params = RelayDTO.Unsubscribe.Request.Params(Topic(topic), SubscriptionId(subscriptionId))
                )

                observeUnsubscribeResult(unsubscribeRequest.id, onResult)
                relayService.unsubscribeRequest(unsubscribeRequest)
            },
            onFailure = { onResult(Result.failure(it)) }
        )
    }

    private fun observeUnsubscribeResult(id: Long, onResult: (Result<Relay.Model.Call.Unsubscribe.Acknowledgement>) -> Unit) {
        scope.launch {
            try {
                withTimeout(RESULT_TIMEOUT) {
                    resultState
                        .filterIsInstance<RelayDTO.Unsubscribe.Result>()
                        .filter { relayResult -> relayResult.id == id }
                        .first { unsubscribeResult ->
                            when (unsubscribeResult) {
                                is RelayDTO.Unsubscribe.Result.Acknowledgement -> onResult(Result.success(unsubscribeResult.toRelay()))
                                is RelayDTO.Unsubscribe.Result.JsonRpcError -> onResult(Result.failure(Throwable(unsubscribeResult.error.errorMessage)))
                            }
                            true
                        }
                }
            } catch (e: TimeoutCancellationException) {
                onResult(Result.failure(e))
                cancelJobIfActive()
            } catch (e: Exception) {
                onResult(Result.failure(e))
                cancelJobIfActive()
            }
        }
    }

    private fun connectAndCallRelay(onConnected: () -> Unit, onFailure: (Throwable) -> Unit) {
        when {
            shouldConnect() -> connect(onConnected, onFailure)
            isConnecting -> awaitConnection(onConnected, onFailure)
            connectionState.value == ConnectionState.Open -> onConnected()
        }
    }

    private fun shouldConnect() = !isConnecting && (connectionState.value is ConnectionState.Closed || connectionState.value is ConnectionState.Idle)
    private fun connect(onConnected: () -> Unit, onFailure: (Throwable) -> Unit) {
        isConnecting = true
        connectionLifecycle.reconnect()
        awaitConnectionWithRetry(
            onConnected = {
                reset()
                onConnected()
            },
            onFailure = { error ->
                reset()
                onFailure(error)
            }
        )
    }

    private fun awaitConnectionWithRetry(onConnected: () -> Unit, onFailure: (Throwable) -> Unit = {}) {
        scope.launch {
            try {
                withTimeout(CONNECTION_TIMEOUT) {
                    connectionState
                        .filter { state -> state != ConnectionState.Idle }
                        .take(4)
                        .onEach { state -> handleRetries(state, onFailure) }
                        .filter { state -> state == ConnectionState.Open }
                        .firstOrNull {
                            onConnected()
                            true
                        }
                }
            } catch (e: TimeoutCancellationException) {
                onFailure(e)
                cancelJobIfActive()
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    onFailure(e)
                }
            }
        }
    }

    private fun awaitConnection(onConnected: () -> Unit, onFailure: (Throwable) -> Unit) {
        scope.launch {
            try {
                withTimeout(CONNECTION_TIMEOUT) {
                    connectionState
                        .filter { state -> state is ConnectionState.Open }
                        .firstOrNull {
                            onConnected()
                            true
                        }
                }
            } catch (e: TimeoutCancellationException) {
                onFailure(e)
                cancelJobIfActive()
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    onFailure(e)
                }
                cancelJobIfActive()
            }
        }
    }

    private fun CoroutineScope.handleRetries(state: ConnectionState, onFailure: (Throwable) -> Unit) {
        if (state is ConnectionState.Closed) {
            if (retryCount == MAX_RETRIES) {
                onFailure(Throwable("Connectivity error, please check your Internet connection and try again"))
                cancelJobIfActive()
            } else {
                connectionLifecycle.reconnect()
                retryCount++
            }
        }
    }

    private fun getError(event: WebSocket.Event): Throwable = when (event) {
        is WebSocket.Event.OnConnectionClosed -> Throwable(event.shutdownReason.reason)
        is WebSocket.Event.OnConnectionFailed -> event.throwable
        else -> Throwable("Unknown")
    }

    private fun publishSubscriptionAcknowledgement(id: Long) {
        val publishRequest = RelayDTO.Subscription.Result.Acknowledgement(id = id, result = true)
        relayService.publishSubscriptionAcknowledgement(publishRequest)
    }

    private fun CoroutineScope.cancelJobIfActive() {
        if (this.coroutineContext.job.isActive) {
            this.coroutineContext.job.cancel()
        }
    }

    private fun reset() {
        isConnecting = false
        retryCount = 0
    }

    private companion object {
        const val REPLAY: Int = 1
        const val RESULT_TIMEOUT: Long = 60000
        const val CONNECTION_TIMEOUT: Long = 15000
        const val MAX_RETRIES: Int = 3
    }
}