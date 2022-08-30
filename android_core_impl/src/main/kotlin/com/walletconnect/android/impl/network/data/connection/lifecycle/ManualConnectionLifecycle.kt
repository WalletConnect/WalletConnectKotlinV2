@file:JvmSynthetic

package com.walletconnect.android.impl.network.data.connection.lifecycle

import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.walletconnect.android.impl.common.scope.scope
import com.walletconnect.android.api.ConnectionEvent
import com.walletconnect.android.api.ConnectionController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class ManualConnectionLifecycle(
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
    }
}