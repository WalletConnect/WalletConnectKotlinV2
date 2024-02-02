package com.walletconnect.web3.modal.ui.components.internal.email.webview

import android.content.Context
import com.walletconnect.foundation.util.Logger

internal class EmailProvider(context: Context, logger: Logger) {

    internal var emailWebView = EmailWebViewWrapper(context, logger).apply {
        //todo refresh webview on activity change somehow
    }

    var context: Context = emailWebView.mutableContext.baseContext
        set(value) {
            emailWebView.newActivityContext(value)
            field = value
        }

}