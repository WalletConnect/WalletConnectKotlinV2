package com.walletconnect.auth.client

import com.walletconnect.utils.Empty
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
            }
        }
    }

    override fun setRequesterDelegate(delegate: AuthInterface.RequesterDelegate) {
    }

    override fun setResponderDelegate(delegate: AuthInterface.ResponderDelegate) {
    }

    override fun pair(pair: Auth.Params.Pair, onError: (Auth.Model.Error) -> Unit) {
        //TODO("Not yet implemented")
    }

    override fun request(params: Auth.Params.Request) {
        //TODO("Not yet implemented")
    }

    override fun respond(params: Auth.Params.Respond) {
        //TODO("Not yet implemented")
    }

    override fun getPendingRequest(): Map<Int, Auth.Model.PendingRequest> {
        //TODO("Not yet implemented")
        return emptyMap()
    }

    override fun getResponse(params: Auth.Params.RequestId): Auth.Model.Response {
        //TODO("Not yet implemented")
        return Auth.Model.Response.ErrorResponse(0, String.Empty)
    }
}