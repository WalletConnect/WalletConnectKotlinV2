@file:JvmSynthetic

package com.walletconnect.android.internal.common.connection

import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.lifecycle.LifecycleRegistry

internal class ManualConnectionLifecycle(
    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(),
) : Lifecycle by lifecycleRegistry {
    fun connect() {
        lifecycleRegistry.onNext(Lifecycle.State.Started)
    }

    fun disconnect() {
        lifecycleRegistry.onNext(Lifecycle.State.Stopped.WithReason())
    }

    fun restart() {
        lifecycleRegistry.onNext(Lifecycle.State.Stopped.WithReason())
        lifecycleRegistry.onNext(Lifecycle.State.Started)
    }
}