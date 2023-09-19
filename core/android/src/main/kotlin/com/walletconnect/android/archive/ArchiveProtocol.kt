package com.walletconnect.android.archive

import com.walletconnect.android.Core
import com.walletconnect.android.archive.domain.GetMessagesUseCase
import com.walletconnect.android.archive.domain.RegisterTagsUseCase
import com.walletconnect.android.archive.network.model.messages.MessagesParams
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.model.ArchiveMessage
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.foundation.util.Logger
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named

class ArchiveProtocol(
    private val koinApp: KoinApplication = wcKoinApp,
) : ArchiveInterface {
    private val registerTagsUseCase: RegisterTagsUseCase by lazy { koinApp.koin.get() }
    private val getMessagesUseCase: GetMessagesUseCase by lazy { koinApp.koin.get() }
    private val logger: Logger by lazy { koinApp.koin.get(named(AndroidCommonDITags.LOGGER)) }
    private val decryptRequestsUseCase: DecryptRequestsUseCase by lazy { koinApp.koin.get() }

    private lateinit var relayServerUrl: String

    override fun initialize(relayServerUrl: String) {
        this.relayServerUrl = relayServerUrl
    }

    override suspend fun registerTags(tags: List<Tags>, onSuccess: () -> Unit, onError: (Core.Model.Error) -> Unit) {
        registerTagsUseCase(tags, relayServerUrl).fold(
            onFailure = { error -> onError(Core.Model.Error(error)) },
            onSuccess = {
                logger.log("Registered in Archive: $tags")
                onSuccess()
            }
        )
    }

    override suspend fun getAllMessages(params: MessagesParams, onSuccess: suspend (Map<ClientJsonRpc, ClientParams>) -> Unit, onError: (Core.Model.Error) -> Unit) {
        val allMessageArchive: MutableList<ArchiveMessage> = mutableListOf()

        suspend fun recursiveOnSuccess(allMessageArchive: MutableList<ArchiveMessage>, justFetchedMessageArchive: List<ArchiveMessage>) {
            allMessageArchive.addAll(justFetchedMessageArchive)
            if (justFetchedMessageArchive.size == params.messageCount) {
                logger.log("Fetched from Archive ${justFetchedMessageArchive.size} messages fetching ${params.messageCount} more")

                val recursiveParams = MessagesParams(params.topic, originId = justFetchedMessageArchive.last().messageId, messageCount = params.messageCount, params.direction)

                getMessagesUseCase(recursiveParams).fold(
                    onFailure = { error -> onError(Core.Model.Error(error)) },
                    onSuccess = { response ->
                        (response.messages ?: emptyList()).also { messages ->
                            recursiveOnSuccess(allMessageArchive, messages)
                        }
                    }
                )
            } else {
                logger.log("Fetched from Archive ${allMessageArchive.size} messages")
                onSuccess(decryptRequestsUseCase(allMessageArchive))
            }
        }

        getMessagesUseCase(params).fold(
            onFailure = { error -> onError(Core.Model.Error(error)) },
            onSuccess = { response ->
                (response.messages ?: emptyList()).also { messages ->
                    recursiveOnSuccess(allMessageArchive, messages)
                }
            }
        )
    }
}


