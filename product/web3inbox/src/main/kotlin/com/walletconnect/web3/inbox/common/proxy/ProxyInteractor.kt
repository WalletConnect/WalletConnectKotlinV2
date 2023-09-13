package com.walletconnect.web3.inbox.common.proxy

import android.os.Handler
import android.os.Looper
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.inbox.common.exception.WebViewIsNullException
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.json_rpc.Web3InboxSerializer
import com.walletconnect.web3.inbox.webview.WebViewWeakReference

internal abstract class ProxyInteractor(
    private val logger: Logger,
    private val webViewWeakReference: WebViewWeakReference,
    private val prefix: String
) {

    abstract fun rpcToWeb3InboxCall(rpc: String): String

    @Throws(WebViewIsNullException::class)
    fun <T : JsonRpcResponse> respond(rpc: T) {
        Handler(Looper.getMainLooper()).post {
            val rpcAsString = runCatching { Web3InboxSerializer.serializeRpc(rpc) }.getOrNull() ?: return@post logger.error("Unable to serialize: $rpc")
            logger.log("Responding for $prefix: $rpcAsString")
            val script = rpcToWeb3InboxCall(rpcAsString)

            try {
                webViewWeakReference.webView.evaluateJavascript(script, null)
            } catch (webViewIsNullException: WebViewIsNullException) {
                logger.error("Unable to respond for $prefix: $rpcAsString")
            }
        }
    }

    @Throws(WebViewIsNullException::class)
    fun <T : Web3InboxRPC.Call> call(rpc: T) {
        Handler(Looper.getMainLooper()).post {
            val rpcAsString = runCatching { Web3InboxSerializer.serializeRpc(rpc) }.getOrNull() ?: return@post logger.error("Unable to serialize: $rpc")
            logger.log("Calling for $prefix: $rpcAsString")
            val script = rpcToWeb3InboxCall(rpcAsString)

            try {
                webViewWeakReference.webView.evaluateJavascript(script, null)
            } catch (webViewIsNullException: WebViewIsNullException) {
                logger.error("Unable to call for $prefix: $script")
            }
        }
    }
}

internal class ChatProxyInteractor(logger: Logger, webViewWeakReference: WebViewWeakReference) : ProxyInteractor(logger, webViewWeakReference, "chat") {
    override fun rpcToWeb3InboxCall(rpc: String): String = "window.web3inbox.chat.postMessage($rpc)"
}

internal class NotifyProxyInteractor(logger: Logger, webViewWeakReference: WebViewWeakReference) : ProxyInteractor(logger, webViewWeakReference, "notify") {
    override fun rpcToWeb3InboxCall(rpc: String): String = "window.web3inbox.notify.postMessage($rpc)"
}