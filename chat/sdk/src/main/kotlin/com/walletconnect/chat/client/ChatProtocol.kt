@file:JvmSynthetic

package com.walletconnect.chat.client

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.chat.client.mapper.toClient
import com.walletconnect.chat.client.mapper.toClientError
import com.walletconnect.chat.client.mapper.toCommon
import com.walletconnect.chat.common.model.Events
import com.walletconnect.chat.di.*
import com.walletconnect.chat.engine.domain.ChatEngine
import com.walletconnect.foundation.common.model.PublicKey
import kotlinx.coroutines.launch

internal class ChatProtocol : ChatInterface {
    private lateinit var chatEngine: ChatEngine

    companion object {
        val instance = ChatProtocol()
    }

    @Throws(IllegalStateException::class)
    override fun initialize(init: Chat.Params.Init, onError: (Chat.Model.Error) -> Unit) {
        try {
            wcKoinApp.run {
                modules(
                    jsonRpcModule(),
                    storageModule(),
                    engineModule(),
                    commonModule()
                )
            }

            chatEngine = wcKoinApp.koin.get()
            chatEngine.setup()
        } catch (e: Exception) {
            onError(Chat.Model.Error(e))
        }
    }

    @Throws(IllegalStateException::class)
    override fun setChatDelegate(delegate: ChatInterface.ChatDelegate): Unit = wrapWithEngineInitializationCheck() {
        scope.launch {
            chatEngine.events.collect { event ->
                when (event) {
                    is Events.OnInvite -> delegate.onInvite(event.toClient())
                    is Events.OnInviteAccepted -> delegate.onInviteAccepted(event.toClient())
                    is Events.OnInviteRejected -> delegate.onInviteRejected(event.toClient())
                    is Events.OnMessage -> delegate.onMessage(event.toClient())
                    is Events.OnLeft -> delegate.onLeft(event.toClient())
                    is ConnectionState -> delegate.onConnectionStateChange(event.toClient())
                    is SDKError -> delegate.onError(event.toClientError())
                }
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun resolve(resolve: Chat.Params.Resolve, listener: Chat.Listeners.Resolve) = protocolFunction(listener::onError) {
        chatEngine.resolveAccount(
            AccountId(resolve.account.value),
            { publicKey -> listener.onSuccess(publicKey) },
            { throwable -> listener.onError(Chat.Model.Error(throwable)) }
        )
    }

    @Throws(IllegalStateException::class)
    override fun goPrivate(goPrivate: Chat.Params.GoPrivate, onSuccess: () -> Unit, onError: (Chat.Model.Error) -> Unit) = protocolFunction(onError) {
        chatEngine.goPrivate(goPrivate.account.toCommon(), { onSuccess() }, { error -> onError(Chat.Model.Error(error)) })
    }

    @Throws(IllegalStateException::class)
    override fun goPublic(goPublic: Chat.Params.GoPublic, onSuccess: (String) -> Unit, onError: (Chat.Model.Error) -> Unit) = protocolFunction(onError) {
        chatEngine.goPublic(goPublic.account.toCommon(), { inviteKey -> onSuccess(inviteKey) }, { error -> onError(Chat.Model.Error(error)) })
    }

    @Throws(IllegalStateException::class)
    override fun invite(invite: Chat.Params.Invite, onSuccess: (Long) -> Unit, onError: (Chat.Model.Error) -> Unit) = protocolFunction(onError) {
        chatEngine.invite(invite.toCommon(), { inviteId -> onSuccess(inviteId) }, { error -> onError(Chat.Model.Error(error)) })
    }

    @Throws(IllegalStateException::class)
    override fun accept(accept: Chat.Params.Accept, onSuccess: (String) -> Unit, onError: (Chat.Model.Error) -> Unit) = protocolFunction(onError) {
        chatEngine.accept(accept.inviteId, { threadTopic -> onSuccess(threadTopic) }, { error -> onError(Chat.Model.Error(error)) })
    }

    @Throws(IllegalStateException::class)
    override fun reject(reject: Chat.Params.Reject, onSuccess: () -> Unit, onError: (Chat.Model.Error) -> Unit) = protocolFunction(onError) {
        chatEngine.reject(reject.inviteId, onSuccess) { error -> onError(Chat.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun message(message: Chat.Params.Message, onSuccess: () -> Unit, onError: (Chat.Model.Error) -> Unit) = protocolFunction(onError) {
        chatEngine.message(message.topic, message.toCommon(), onSuccess) { error -> onError(Chat.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun ping(ping: Chat.Params.Ping, onSuccess: (String) -> Unit, onError: (Chat.Model.Error) -> Unit) = protocolFunction(onError) {
        chatEngine.ping(ping.topic, onSuccess = { topic -> onSuccess(topic) }, { error -> onError(Chat.Model.Error(error)) })
    }

    @Throws(IllegalStateException::class)
    override fun leave(leave: Chat.Params.Leave, onError: (Chat.Model.Error) -> Unit) = protocolFunction(onError) {
        chatEngine.leave(leave.topic) { error -> onError(Chat.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun setContact(setContact: Chat.Params.SetContact, onError: (Chat.Model.Error) -> Unit) = protocolFunction(onError) {
        chatEngine.setContact(setContact.account.toCommon(), PublicKey(setContact.publicKey)) { error ->
            onError(Chat.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun getReceivedInvites(getReceivedInvites: Chat.Params.GetReceivedInvites): Map<Long, Chat.Model.Invite.Received> = wrapWithEngineInitializationCheck() {
        chatEngine.getReceivedInvites(getReceivedInvites.account.value).mapValues { (_, invite) -> invite.toClient() }
    }

    @Throws(IllegalStateException::class)
    override fun getSentInvites(getSentInvites: Chat.Params.GetSentInvites): Map<Long, Chat.Model.Invite.Sent> = wrapWithEngineInitializationCheck() {
        chatEngine.getSentInvites(getSentInvites.account.value).mapValues { (_, invite) -> invite.toClient() }
    }

    @Throws(IllegalStateException::class)
    override fun getThreads(getThreads: Chat.Params.GetThreads): Map<String, Chat.Model.Thread> = wrapWithEngineInitializationCheck() {
        chatEngine.getThreadsByAccount(getThreads.account.value).mapValues { (_, thread) -> thread.toClient() }
    }

    @Throws(IllegalStateException::class)
    override fun getMessages(getMessages: Chat.Params.GetMessages): List<Chat.Model.Message> = wrapWithEngineInitializationCheck() {
        chatEngine.getMessagesByTopic(getMessages.topic).map { message -> message.toClient() }
    }

    @Throws(IllegalStateException::class)
    override fun register(register: Chat.Params.Register, listener: Chat.Listeners.Register) = protocolFunction(listener::onError) {
        chatEngine.registerIdentity(
            register.account.toCommon(),
            onSign = { message -> listener.onSign(message).toCommon() },
            { didKey -> listener.onSuccess(didKey) },
            { throwable -> listener.onError(Chat.Model.Error(throwable)) },
            register.private
        )
    }

    @Throws(IllegalStateException::class)
    override fun unregister(unregister: Chat.Params.Unregister, listener: Chat.Listeners.Unregister) = protocolFunction(listener::onError) {
        chatEngine.unregisterIdentity(
            unregister.account.toCommon(),
            { message -> listener.onSign(message).toCommon() },
            { didKey -> listener.onSuccess(didKey) },
            { throwable -> listener.onError(Chat.Model.Error(throwable)) },
        )
    }


    @Throws(IllegalStateException::class)
    private fun <R> wrapWithEngineInitializationCheck(block: () -> R): R {
        check(::chatEngine.isInitialized) {
            "ChatClient needs to be initialized first using the initialize function"
        }
        return block()
    }

    private fun wrapWithRunCatching(onError: (Chat.Model.Error) -> Unit, block: () -> Unit) = runCatching(block).onFailure { error -> onError(Chat.Model.Error(error)) }

    @Throws(IllegalStateException::class)
    private fun protocolFunction(onError: (Chat.Model.Error) -> Unit, block: () -> Unit) {
        wrapWithEngineInitializationCheck() {
            wrapWithRunCatching(onError) { block() }
        }
    }
}