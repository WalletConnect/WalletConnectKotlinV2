package com.walletconnect.push.dapp.client

import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.push.common.Push
import com.walletconnect.push.dapp.di.engineModule
import com.walletconnect.push.dapp.engine.domain.DappEngine

internal class DappProtocol: DappInterface {
    private lateinit var dappEngine: DappEngine

    companion object {
        val instance = DappProtocol()
    }

    override fun initialize(init: Push.Params.Init, onError: (Push.Model.Error) -> Unit) {
        try {
            with(init) {
                wcKoinApp.modules(
                    engineModule()
                )
            }

            dappEngine = wcKoinApp.koin.get()
            dappEngine.setup()
        } catch (e: Exception) {
            onError(Push.Model.Error(e))
        }
    }

    override fun request(params: Push.Params.Request, onSuccess: (Push.Model.RequestId) -> Unit, onError: (Push.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun notify(params: Push.Params.Notify, onError: (Push.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getActiveSubscriptions(): Map<String, Push.Model.Subscription> {
        TODO("Not yet implemented")
    }

    override fun delete(params: Push.Params.Delete, onError: (Push.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }


}