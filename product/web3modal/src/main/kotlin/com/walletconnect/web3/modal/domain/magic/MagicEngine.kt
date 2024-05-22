package com.walletconnect.web3.modal.domain.magic

import com.walletconnect.web3.modal.domain.magic.model.MagicEvent
import com.walletconnect.web3.modal.domain.magic.model.MagicRequest
import com.walletconnect.web3.modal.ui.components.internal.email.webview.MagicWebViewManager
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

internal class MagicEngine(private val magicWebViewManager: MagicWebViewManager) {
    internal suspend inline fun <reified T : MagicEvent> sendMessage(message: MagicRequest): T {
        magicWebViewManager.sendMessage(message)
        return subscribeToEvent<T>()
    }

    private suspend inline fun <reified T : MagicEvent> subscribeToEvent(): T {
        return withTimeout(5000L) {
            suspendCancellableCoroutine { continuation ->
                val job = launch {
                    magicWebViewManager.eventFlow
                        .filterIsInstance<T>()
                        .collect { continuation.resume(it) }
                }
                continuation.invokeOnCancellation { job.cancel() }
            }
        }
    }
}