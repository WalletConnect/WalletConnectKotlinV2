package com.walletconnect.chat.client

import com.walletconnect.chat.engine.domain.EngineInteractor
import org.koin.core.KoinApplication

internal class ChatProtocol : ChatInterface {
    private val wcKoinApp: KoinApplication = KoinApplication.init()
    private lateinit var engineInteractor: EngineInteractor
//    override val relay: Relay by lazy { wcKoinApp.koin.get() } TODO: Figure out how to get relay here

    companion object {
        val instance = ChatProtocol()
    }

    @Throws(IllegalStateException::class)
    override fun initialize(init: Chat.Params.Init, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()
        TODO("Not yet implemented")
    }

    @Throws(IllegalStateException::class)
    override fun register(register: Chat.Params.Register, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()
        TODO("Not yet implemented")
    }

    @Throws(IllegalStateException::class)
    override fun resolve(resolve: Chat.Params.Resolve, onError: (Chat.Model.Error) -> Unit) {
        checkEngineInitialization()
        TODO("Not yet implemented")
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
//        check(::engineInteractor.isInitialized) {
//            "ChatClient needs to be initialized first using the initialize function"
//        }
    }
}