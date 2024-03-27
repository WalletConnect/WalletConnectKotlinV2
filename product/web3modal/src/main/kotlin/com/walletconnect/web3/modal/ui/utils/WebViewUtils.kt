package com.walletconnect.web3.modal.ui.utils

import android.webkit.WebView
import com.walletconnect.web3.modal.domain.magic.model.MagicRequest

internal const val WEB_APP_INTERFACE = "w3mWebview"
private const val SEND_MESSAGE = "sendMessage"

private val SEND_MESSAGE_SCRIPT = """
        window.addEventListener('message', function({ data }) { window.$WEB_APP_INTERFACE.postMessage(JSON.stringify(data)); });
        const sendMessage = async (message) => { postMessage(message, '*'); };
        """.trimIndent()

internal fun WebView.injectSendMessageScript() {
    evaluateJavascript(SEND_MESSAGE_SCRIPT, null)
}

internal fun WebView.sendMethod(request: MagicRequest) {
    evaluateJavascript("$SEND_MESSAGE($request)", null)
}