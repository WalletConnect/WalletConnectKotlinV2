package com.walletconnect.web3.modal.ui.components.internal.email.webview

import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import com.walletconnect.foundation.util.Logger

internal class EmailMagicChromeClient(
    private val logger: Logger
) : WebChromeClient() {

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        if (consoleMessage?.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
            logger.error("EmailMagicChromeClient: ${consoleMessage.message()}")
        }
        return super.onConsoleMessage(consoleMessage)
    }

}
