package com.walletconnect.web3.modal.ui.components.internal.email

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.walletconnect.web3.modal.ui.components.internal.email.webview.MagicWebViewManager

internal class ActivityLifeCycleWatcher(
    application: Application,
    private val magicWebViewManager: MagicWebViewManager
) {
    init {
        application.registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks())
    }

    private inner class ActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, p1: Bundle?) {
            magicWebViewManager.updateWebView(activity)
        }

        override fun onActivityStarted(p0: Activity) {}
        override fun onActivityResumed(p0: Activity) {}
        override fun onActivityPaused(p0: Activity) {}
        override fun onActivityStopped(p0: Activity) {}
        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
        override fun onActivityDestroyed(p0: Activity) {}
    }
}