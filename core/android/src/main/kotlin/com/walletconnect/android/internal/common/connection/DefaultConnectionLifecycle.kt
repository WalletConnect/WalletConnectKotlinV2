@file:JvmSynthetic

package com.walletconnect.android.internal.common.connection

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.ShutdownReason
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.walletconnect.foundation.network.ConnectionLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

internal class DefaultConnectionLifecycle(
    application: Application,
    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry()
) : Lifecycle by lifecycleRegistry, ConnectionLifecycle {
    private val job = SupervisorJob()
    private var scope = CoroutineScope(job + Dispatchers.Default)

    private val _onResume = MutableStateFlow<Boolean?>(null)
    override val onResume: StateFlow<Boolean?> = _onResume.asStateFlow()

    init {
        application.registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks())
    }

    override fun reconnect() {
        lifecycleRegistry.onNext(Lifecycle.State.Stopped.WithReason())
        lifecycleRegistry.onNext(Lifecycle.State.Started)
    }

    private inner class ActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        var isResumed: Boolean = false
        var job: Job? = null

        override fun onActivityPaused(activity: Activity) {
            isResumed = false

            job = scope.launch {
                delay(TimeUnit.SECONDS.toMillis(30))
                if (!isResumed) {
                    lifecycleRegistry.onNext(Lifecycle.State.Stopped.WithReason(ShutdownReason(1000, "App is paused")))
                    job = null
                    _onResume.value = false
                }
            }
        }

        override fun onActivityResumed(activity: Activity) {
            isResumed = true

            if (job?.isActive == true) {
                job?.cancel()
                job = null
            }


            scope.launch {
                _onResume.value = true
            }
        }

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityDestroyed(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    }
}