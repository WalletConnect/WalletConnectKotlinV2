@file:JvmSynthetic

package com.walletconnect.android.internal.common.connection

import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.walletconnect.foundation.network.ConnectionLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class ManualConnectionLifecycle(
    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(),
) : Lifecycle by lifecycleRegistry, ConnectionLifecycle {
    fun connect() {
        lifecycleRegistry.onNext(Lifecycle.State.Started)
    }

    fun disconnect() {
        lifecycleRegistry.onNext(Lifecycle.State.Stopped.WithReason())
    }

    override val onResume: StateFlow<Boolean?>
        get() = MutableStateFlow(null)

    override fun reconnect() {
        lifecycleRegistry.onNext(Lifecycle.State.Stopped.WithReason())
        lifecycleRegistry.onNext(Lifecycle.State.Started)
    }
}