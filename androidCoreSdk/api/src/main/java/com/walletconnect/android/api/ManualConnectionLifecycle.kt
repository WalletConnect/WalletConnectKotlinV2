@file:JvmSynthetic

package com.walletconnect.android.api

import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.walletconnect.foundation.network.data.ConnectionController
import com.walletconnect.foundation.network.data.ConnectionEvent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ManualConnectionLifecycle(
    connectionController: ConnectionController,
    private val lifecycleRegistry: LifecycleRegistry,
) : Lifecycle by lifecycleRegistry {

    init {
        if (connectionController is ConnectionController.Manual) {
            connectionController.connectionEventFlow
                .onEach { event ->
                    when (event) {
                        ConnectionEvent.CONNECT -> lifecycleRegistry.onNext(Lifecycle.State.Started)
                        ConnectionEvent.DISCONNECT -> lifecycleRegistry.onNext(Lifecycle.State.Stopped.WithReason())
                    }
                }
                .launchIn(scope)
        }
//        else {
//            var app: Application? = null
//            app?.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks{
//
//            })
//        }
    }
}