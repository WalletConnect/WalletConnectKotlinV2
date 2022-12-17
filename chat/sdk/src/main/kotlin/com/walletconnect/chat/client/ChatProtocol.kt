@file:JvmSynthetic

package com.walletconnect.chat.client

import com.walletconnect.android.impl.common.SDKError
import com.walletconnect.android.impl.common.model.ConnectionState
import com.walletconnect.android.impl.di.cryptoModule
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.chat.client.mapper.toClient
import com.walletconnect.chat.client.mapper.toClientError
import com.walletconnect.chat.client.mapper.toCommon
import com.walletconnect.chat.client.mapper.toEngineDO
import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.chat.common.model.AccountIdWithPublicKey
import com.walletconnect.chat.di.*
import com.walletconnect.chat.engine.domain.ChatEngine
import com.walletconnect.chat.engine.model.EngineDO
import com.walletconnect.foundation.common.model.PublicKey
import kotlinx.coroutines.launch

internal class ChatProtocol : ChatInterface {
    private val keyServerUrl = "https://keys.walletconnect.com"
    private lateinit var chatEngine: ChatEngine

    companion object {
        val instance = ChatProtocol()
        const val storageSuffix: String = "_chat"
    }

    @Throws(IllegalStateException::class)
    override fun initialize(init: Chat.Params.Init, onError: (Chat.Model.Error) -> Unit) {
        try {
            wcKoinApp.run {
                modules(
                    cryptoModule(),
                    keyServerModule(keyServerUrl),
                    jsonRpcModule(),
                    storageModule(storageSuffix),
                    engineModule()
                )
            }

            chatEngine = wcKoinApp.koin.get()
            chatEngine.setup()
        } catch (e: Exception) {
            onError(Chat.Model.Error(e))
        }
    }

    override fun setChatDelegate(delegate: ChatInterface.ChatDelegate) {
        checkEngineInitialization()

        scope.launch {
            chatEngine.events.collect { event ->
                when (event) {
                    is EngineDO.Events.OnInvite -> delegate.onInvite(event.toClient())
                    is EngineDO.Events.OnJoined -> delegate.onJoined(event.toClient())
                    is EngineDO.Events.OnReject -> delegate.onReject(event.toClient())
                    is EngineDO.Events.OnMessage -> delegate.onMessage(event.toClient())
                    is EngineDO.Events.OnLeft -> delegate.onLeft(event.toClient())
                    is ConnectionState -> delegate.onConnectionStateChange(event.toClient())
                    is SDKError -> delegate.onError(event.toClientError())
                }
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun register(register: Chat.Params.Register, listener: Chat.Listeners.Register) {
        checkEngineInitialization()

        chatEngine.registerAccount(
            AccountId(register.account.value),
            { publicKey -> listener.onSuccess(publicKey) },
            { throwable -> listener.onError(Chat.Model.Error(throwable)) },
            register.private ?: false
        )
    }

    @Throws(IllegalStateException::class)
    override fun resolve(resolve: Chat.Params.Resolve, listener: Chat.Listeners.Resolve) {
        checkEngineInitialization()

        chatEngine.resolveAccount(
            AccountId(resolve.account.value),
            { publicKey -> listener.onSuccess(publicKey) },
            { throwable -> listener.onError(Chat.Model.Error(throwable)) }
        )
    }

    @Throws(IllegalStateException::class)
    override fun invite(invite: Chat.Params.Invite, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()

        chatEngine.invite(invite.account.toCommon(), invite.toEngineDO()) { error -> onError(Chat.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun accept(accept: Chat.Params.Accept, onSuccess: (String) -> Unit, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()

        chatEngine.accept(accept.inviteId, { threadTopic -> onSuccess(threadTopic) }, { error -> onError(Chat.Model.Error(error)) })
    }

    @Throws(IllegalStateException::class)
    override fun reject(reject: Chat.Params.Reject, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()

        chatEngine.reject(reject.inviteId) { error -> onError(Chat.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun message(message: Chat.Params.Message, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()

        chatEngine.message(message.topic, message.toEngineDO()) { error -> onError(Chat.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun ping(ping: Chat.Params.Ping, onSuccess: (String) -> Unit, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()

        chatEngine.ping(ping.topic, onSuccess = { topic -> onSuccess(topic) }, { error -> onError(Chat.Model.Error(error)) })
    }

    @Throws(IllegalStateException::class)
    override fun leave(leave: Chat.Params.Leave, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()

        chatEngine.leave(leave.topic) { error -> onError(Chat.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun addContact(addContact: Chat.Params.AddContact, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()
        chatEngine.addContact(AccountIdWithPublicKey(addContact.account.toCommon(), PublicKey(addContact.publicKey))) { error ->
            onError(Chat.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun getInvites(getInvites: Chat.Params.GetInvites): Map<String, Chat.Model.Invite> {
        checkEngineInitialization()
        TODO("Not yet implemented")
    }

    @Throws(IllegalStateException::class)
    override fun getThreads(getThreads: Chat.Params.GetThreads): Map<String, Chat.Model.Thread> {
        checkEngineInitialization()
        TODO("Not yet implemented")
    }

    @Throws(IllegalStateException::class)
    override fun getMessages(getMessages: Chat.Params.GetMessages): List<Chat.Model.Message> {
        checkEngineInitialization()
        TODO("Not yet implemented")
    }

    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::chatEngine.isInitialized) {
            "ChatClient needs to be initialized first using the initialize function"
        }
    }
}