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

    override fun initialize(init: Push.Dapp.Params.Init, onError: (Push.Dapp.Model.Error) -> Unit) {
        try {
            with(init) {
                wcKoinApp.modules(
                    engineModule()
                )
            }

            dappEngine = wcKoinApp.koin.get()
            dappEngine.setup()
        } catch (e: Exception) {
            onError(Push.Dapp.Model.Error(e))
        }
    }

    override fun request(params: Push.Dapp.Params.Request, onSuccess: (Push.Dapp.Model.RequestId) -> Unit, onError: (Push.Dapp.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun notify(params: Push.Dapp.Params.Notify, onError: (Push.Dapp.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getActiveSubscriptions(): Map<String, Push.Dapp.Model.Subscription> {
        TODO("Not yet implemented")
    }

    override fun delete(params: Push.Dapp.Params.Delete, onError: (Push.Dapp.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }


}