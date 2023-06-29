package com.walletconnect.android.history

import com.walletconnect.android.Core
import com.walletconnect.android.history.domain.GetMessagesUseCase
import com.walletconnect.android.history.domain.RegisterTagsUseCase
import com.walletconnect.android.history.network.model.messages.MessagesParams
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.model.HistoryMessage
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.foundation.util.Logger
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named

class HistoryProtocol(
    private val koinApp: KoinApplication = wcKoinApp,
) : HistoryInterface {
    private val registerTagsUseCase: RegisterTagsUseCase by lazy { koinApp.koin.get() }
    private val getMessagesUseCase: GetMessagesUseCase by lazy { koinApp.koin.get() }
    private val logger: Logger by lazy { koinApp.koin.get(named(AndroidCommonDITags.LOGGER)) }
    private val historyMessageNotifier: HistoryMessageNotifier by lazy { koinApp.koin.get() }

    private lateinit var relayServerUrl: String

    override fun initialize(relayServerUrl: String) {
        this.relayServerUrl = relayServerUrl
    }

    override suspend fun registerTags(tags: List<Tags>, onSuccess: () -> Unit, onError: (Core.Model.Error) -> Unit) {
        registerTagsUseCase(tags, relayServerUrl).fold(
            onFailure = { error -> onError(Core.Model.Error(error)) },
            onSuccess = { onSuccess().also { logger.log("Registered: $tags") } }
        )
    }

    override suspend fun getMessages(params: MessagesParams, onSuccess: (List<HistoryMessage>) -> Unit, onError: (Core.Model.Error) -> Unit) {
        getMessagesUseCase(params).fold(
            onFailure = { error -> onError(Core.Model.Error(error)) },
            onSuccess = { response ->
                (response.messages ?: emptyList()).also { messages ->
                    messages.onEach { request -> historyMessageNotifier.requestsSharedFlow.emit(request.toRelay()) }
                    onSuccess(messages).also { logger.log("Fetched ${messages.size} messages") }
                }
            }
        )
    }
}

