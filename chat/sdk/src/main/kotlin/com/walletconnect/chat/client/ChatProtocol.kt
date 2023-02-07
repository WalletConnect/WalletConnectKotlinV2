@file:JvmSynthetic

package com.walletconnect.chat.client

import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.chat.client.mapper.toClient
import com.walletconnect.chat.client.mapper.toClientError
import com.walletconnect.chat.client.mapper.toCommon
import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.chat.common.model.Events
import com.walletconnect.chat.di.*
import com.walletconnect.chat.engine.domain.ChatEngine
import com.walletconnect.foundation.common.model.PublicKey
import kotlinx.coroutines.launch

internal class ChatProtocol : ChatInterface {
    private val keyServerUrl = "https://staging.keys.walletconnect.com"
    private lateinit var chatEngine: ChatEngine

    companion object {
        val instance = ChatProtocol()
    }

    @Throws(IllegalStateException::class)
    override fun initialize(init: Chat.Params.Init, onError: (Chat.Model.Error) -> Unit) {
        try {
            wcKoinApp.run {
                modules(
                    keyServerModule(keyServerUrl),
                    jsonRpcModule(),
                    storageModule(),
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
                    is Events.OnInvite -> delegate.onInvite(event.toClient())
                    is Events.OnJoined -> delegate.onJoined(event.toClient())
                    is Events.OnReject -> delegate.onReject(event.toClient())
                    is Events.OnMessage -> delegate.onMessage(event.toClient())
                    is Events.OnLeft -> delegate.onLeft(event.toClient())
                    is ConnectionState -> delegate.onConnectionStateChange(event.toClient())
                    is SDKError -> delegate.onError(event.toClientError())
                }
            }
        }
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

    override fun goPrivate(goPrivate: Chat.Params.GoPrivate, onSuccess: () -> Unit, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()

        chatEngine.goPrivate(goPrivate.account.toCommon(), { onSuccess() }, { error -> onError(Chat.Model.Error(error)) })
    }

    override fun goPublic(goPublic: Chat.Params.GoPublic, onSuccess: (String) -> Unit, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()

        chatEngine.goPublic(goPublic.account.toCommon(), { inviteKey -> onSuccess(inviteKey) }, { error -> onError(Chat.Model.Error(error)) })
    }

    @Throws(IllegalStateException::class)
    override fun invite(invite: Chat.Params.Invite, onSuccess: (Long) -> Unit, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()

        chatEngine.invite(invite.toCommon(), { inviteId -> onSuccess(inviteId) }, { error -> onError(Chat.Model.Error(error)) })
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

        chatEngine.message(message.topic, message.toCommon()) { error -> onError(Chat.Model.Error(error)) }
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
    override fun setContact(setContact: Chat.Params.SetContact, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()
        chatEngine.addContact(setContact.account.toCommon(), PublicKey(setContact.publicKey)) { error ->
            onError(Chat.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun getReceivedInvites(getReceivedInvites: Chat.Params.GetReceivedInvites): Map<Long, Chat.Model.ReceivedInvite> {
        checkEngineInitialization()
        TODO("Not yet implemented")
    }

    override fun getSentInvites(getSentInvites: Chat.Params.GetSentInvites): Map<Long, Chat.Model.SentInvite> {
        checkEngineInitialization()
        TODO("Not yet implemented")
    }

    @Throws(IllegalStateException::class)
    override fun getThreads(getThreads: Chat.Params.GetThreads): Map<String, Chat.Model.Thread> {
        checkEngineInitialization()

        return chatEngine.getThreadsByAccount(getThreads.account.value).mapValues { (_, thread) -> thread.toClient() }
    }

    @Throws(IllegalStateException::class)
    override fun getMessages(getMessages: Chat.Params.GetMessages): List<Chat.Model.Message> {
        checkEngineInitialization()
        TODO("Not yet implemented")
    }

    @Throws(IllegalStateException::class)
    override fun register(register: Chat.Params.Register, listener: Chat.Listeners.Register) {
        checkEngineInitialization()

        chatEngine.registerIdentity(
            register.account.toCommon(),
            { message -> listener.onSign(message).toCommon() },
            { didKey -> listener.onSuccess(didKey) },
            { throwable -> listener.onError(Chat.Model.Error(throwable)) },
            register.private ?: false
        )
    }

    override fun unregister(unregister: Chat.Params.Unregister, listener: Chat.Listeners.Unregister) {
        checkEngineInitialization()

        chatEngine.unregisterIdentity(
            unregister.account.toCommon(),
            { message -> listener.onSign(message).toCommon() },
            { didKey -> listener.onSuccess(didKey) },
            { throwable -> listener.onError(Chat.Model.Error(throwable)) },
        )
    }

    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::chatEngine.isInitialized) {
            "ChatClient needs to be initialized first using the initialize function"
        }
    }
}