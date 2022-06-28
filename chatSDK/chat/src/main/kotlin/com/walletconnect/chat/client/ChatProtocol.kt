package com.walletconnect.chat.client

import com.walletconnect.chat.copiedFromSign.core.scope.scope
import com.walletconnect.chat.copiedFromSign.di.cryptoManager
import com.walletconnect.chat.copiedFromSign.di.networkModule
import com.walletconnect.chat.copiedFromSign.di.relayerModule
import com.walletconnect.chat.copiedFromSign.di.storageModule
import com.walletconnect.chat.copiedFromSign.relay.data.connection.ConnectionType
import com.walletconnect.chat.core.model.vo.AccountId
import com.walletconnect.chat.core.model.vo.EventsVO
import com.walletconnect.chat.di.engineModule
import com.walletconnect.chat.di.keyServerModule
import com.walletconnect.chat.engine.domain.ChatEngine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication

internal class ChatProtocol : ChatInterface {
    private val wcKoinApp: KoinApplication = KoinApplication.init()
    private lateinit var chatEngine: ChatEngine
//    override val relay: Relay by lazy { wcKoinApp.koin.get() } TODO: Figure out how to get relay as in Sign in here

    private val serverUrl: String = "wss://relay.walletconnect.com?projectId=2ee94aca5d98e6c05c38bce02bee952a"

    companion object {
        val instance = ChatProtocol()
    }

    @Throws(IllegalStateException::class)
    override fun initialize(init: Chat.Params.Init, onError: (Chat.Model.Error) -> Unit) {
        with(init) {
            wcKoinApp.run {
                androidContext(application)
                modules(
                    cryptoManager(), // TODO: Maybe rename to cryptoModule?
                    keyServerModule(keyServerUrl),
//                    TODO: Figure out how to get relay as in Sign in here
//                    networkModule(serverUrl, relay, connectionType.toRelayConnectionType()),
                    //todo: add serverUrl as init param
                    networkModule(serverUrl, ConnectionType.MANUAL),
                    relayerModule(),
                    storageModule(),
                    engineModule()
                )
            }
        }

        chatEngine = wcKoinApp.koin.get()
    }

    override fun setChatDelegate(delegate: ChatInterface.ChatDelegate) {
        checkEngineInitialization()

        scope.launch {
            chatEngine.events.collect { event ->
                when (event) {
                    is EventsVO.OnInvite -> TODO()
                    is EventsVO.OnJoined -> TODO()
                    is EventsVO.OnLeft -> TODO()
                    is EventsVO.OnMessage -> TODO()
                }
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun register(register: Chat.Params.Register, listener: Chat.Listeners.Register) {
        checkEngineInitialization()

        chatEngine.registerAccount(
            AccountId(register.account),
            { publicKey -> listener.onSuccess(publicKey) },
            { throwable -> listener.onError(Chat.Model.Error(throwable)) },
            register.private ?: false
        )
    }

    @Throws(IllegalStateException::class)
    override fun resolve(resolve: Chat.Params.Resolve, listener: Chat.Listeners.Resolve) {
        checkEngineInitialization()

        chatEngine.resolveAccount(
            AccountId(resolve.account),
            { publicKey -> listener.onSuccess(publicKey) },
            { throwable -> listener.onError(Chat.Model.Error(throwable)) }
        )
    }

    @Throws(IllegalStateException::class)
    override fun invite(invite: Chat.Params.Invite, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()
        TODO("Not yet implemented")
    }

    @Throws(IllegalStateException::class)
    override fun accept(accept: Chat.Params.Accept, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()
        TODO("Not yet implemented")
    }

    @Throws(IllegalStateException::class)
    override fun reject(reject: Chat.Params.Reject, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()
        TODO("Not yet implemented")
    }

    @Throws(IllegalStateException::class)
    override fun message(message: Chat.Params.Message, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()
        TODO("Not yet implemented")
    }

    @Throws(IllegalStateException::class)
    override fun ping(ping: Chat.Params.Ping, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()
        TODO("Not yet implemented")
    }

    @Throws(IllegalStateException::class)
    override fun leave(leave: Chat.Params.Leave, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()
        TODO("Not yet implemented")
    }

    @Throws(IllegalStateException::class)
    override fun addContact(addContact: Chat.Params.AddContact, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()
        TODO("Not yet implemented")
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