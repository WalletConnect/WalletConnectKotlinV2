package com.walletconnect.chat.client

import com.walletconnect.chat.client.mapper.toClient
import com.walletconnect.chat.client.mapper.toEngineDO
import com.walletconnect.chat.client.mapper.toVO
import com.walletconnect.chat.copiedFromSign.core.model.vo.PublicKey
import com.walletconnect.chat.copiedFromSign.core.scope.scope
import com.walletconnect.chat.copiedFromSign.di.*
import com.walletconnect.chat.core.model.vo.AccountIdVO
import com.walletconnect.chat.core.model.vo.AccountIdWithPublicKeyVO
import com.walletconnect.chat.di.engineModule
import com.walletconnect.chat.di.keyServerModule
import com.walletconnect.chat.engine.domain.ChatEngine
import com.walletconnect.chat.engine.model.EngineDO
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication

internal class ChatProtocol : ChatInterface {
    private val wcKoinApp: KoinApplication = KoinApplication.init()
    private lateinit var chatEngine: ChatEngine
//    val relay: Relay by lazy { wcKoinApp.koin.get() } //TODO: Figure out how to get relay as in Sign in here

    companion object {
        val instance = ChatProtocol()
    }

    @Throws(IllegalStateException::class)
    override fun initialize(init: Chat.Params.Init, onError: (Chat.Model.Error) -> Unit) {
        with(init) {
            wcKoinApp.run {
                androidContext(application)
                modules(
                    commonModule(),
                    cryptoModule(),
                    keyServerModule(keyServerUrl),
//                    TODO: Figure out how to get relay as in Sign in here
//                    networkModule(serverUrl, relay, connectionType.toRelayConnectionType()),
                    //todo: add serverUrl as init param
                    networkModule("serverUrl"), //TODO: refactor, network module should be initialized in RelayClient
                    relayerModule(),
                    storageModule(),
                    com.walletconnect.chat.di.storageModule(), // TODO: Refactor storage module into one
                    engineModule()
                )

                chatEngine = koin.get()
            }
        }
    }

    override fun setChatDelegate(delegate: ChatInterface.ChatDelegate) {
        checkEngineInitialization()

        scope.launch {
            chatEngine.events.collect { event ->
                when (event) {
                    is EngineDO.Events.OnInvite -> delegate.onInvite(event.toClient())
                    is EngineDO.Events.OnJoined -> delegate.onJoined(event.toClient())
                    is EngineDO.Events.OnLeft -> Unit
                    is EngineDO.Events.OnMessage -> delegate.onMessage(event.toClient())
                }
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun register(register: Chat.Params.Register, listener: Chat.Listeners.Register) {
        checkEngineInitialization()

        chatEngine.registerAccount(
            AccountIdVO(register.account.value),
            { publicKey -> listener.onSuccess(publicKey) },
            { throwable -> listener.onError(Chat.Model.Error(throwable)) },
            register.private ?: false
        )
    }

    @Throws(IllegalStateException::class)
    override fun resolve(resolve: Chat.Params.Resolve, listener: Chat.Listeners.Resolve) {
        checkEngineInitialization()

        chatEngine.resolveAccount(
            AccountIdVO(resolve.account.value),
            { publicKey -> listener.onSuccess(publicKey) },
            { throwable -> listener.onError(Chat.Model.Error(throwable)) }
        )
    }

    @Throws(IllegalStateException::class)
    override fun invite(invite: Chat.Params.Invite, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()

        chatEngine.invite(invite.account.toVO(), invite.toEngineDO()) { error -> onError(Chat.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun accept(accept: Chat.Params.Accept, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()

        chatEngine.accept(accept.inviteId) { error -> onError(Chat.Model.Error(error)) }
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
    override fun ping(ping: Chat.Params.Ping, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()

        chatEngine.ping(ping.topic) { error -> onError(Chat.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun leave(leave: Chat.Params.Leave, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()

        chatEngine.leave(leave.topic) { error -> onError(Chat.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun addContact(addContact: Chat.Params.AddContact, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()
        chatEngine.addContact(AccountIdWithPublicKeyVO(addContact.account.toVO(), PublicKey(addContact.publicKey))) { error ->
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