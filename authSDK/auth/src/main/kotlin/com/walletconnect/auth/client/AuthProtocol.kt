package com.walletconnect.auth.client

import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication

internal class AuthProtocol : AuthInterface {
    private val wcKoinApp: KoinApplication = KoinApplication.init()
//    private lateinit var chatEngine: ChatEngine
//    val relay: Relay by lazy { wcKoinApp.koin.get() } //TODO: Figure out how to get relay as in Sign in here

    companion object {
        val instance = AuthProtocol()
    }

    @Throws(IllegalStateException::class)
    override fun initialize(init: Auth.Params.Init, onError: (Auth.Model.Error) -> Unit) {
        with(init) {
            wcKoinApp.run {
                androidContext(application)
                modules()

//                chatEngine = koin.get()
            }
        }
    }

    override fun setAuthDelegate(delegate: AuthInterface.AuthDelegate) {
//        checkEngineInitialization()
//
//        scope.launch {
//            chatEngine.events.collect { event ->
//                when (event) {
//                    is EngineDO.Events.OnInvite -> delegate.onInvite(event.toClient())
//                    is EngineDO.Events.OnJoined -> delegate.onJoined(event.toClient())
//                    is EngineDO.Events.OnLeft -> Unit
//                    is EngineDO.Events.OnMessage -> delegate.onMessage(event.toClient())
//                }
//            }
//        }
    }

//
//    @Throws(IllegalStateException::class)
//    private fun checkEngineInitialization() {
//        check(::chatEngine.isInitialized) {
//            "ChatClient needs to be initialized first using the initialize function"
//        }
//    }
}