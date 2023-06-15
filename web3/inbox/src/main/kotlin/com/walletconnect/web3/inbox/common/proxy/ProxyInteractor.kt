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
) {

    abstract fun rpcToWeb3InboxCall(rpc: String): String

    @Throws(WebViewIsNullException::class)
    fun <T : JsonRpcResponse> respond(rpc: T) {
        Handler(Looper.getMainLooper()).post {
            val rpcAsString = Web3InboxSerializer.serializeRpc(rpc) ?: return@post logger.error("Unable to serialize: $rpc")
            logger.log("Responding: $rpcAsString")
            val script = rpcToWeb3InboxCall(rpcAsString)

            try {
                webViewWeakReference.webView.evaluateJavascript(script, null)
            } catch (webViewIsNullException: WebViewIsNullException) {
                logger.error("Unable to call: $rpcAsString")
            }
        }
    }

    @Throws(WebViewIsNullException::class)
    fun <T : Web3InboxRPC.Call> call(rpc: T) {
        Handler(Looper.getMainLooper()).post {
            val rpcAsString = Web3InboxSerializer.serializeRpc(rpc) ?: return@post logger.error("Unable to serialize: $rpc")
            logger.log("Calling: $rpcAsString")
            val script = rpcToWeb3InboxCall(rpcAsString)

            try {
                webViewWeakReference.webView.evaluateJavascript(script, null)
            } catch (webViewIsNullException: WebViewIsNullException) {
                logger.error("Unable to call: $rpcAsString")
            }
        }
    }
}

internal class ChatProxyInteractor(logger: Logger, webViewWeakReference: WebViewWeakReference) : ProxyInteractor(logger, webViewWeakReference) {
    override fun rpcToWeb3InboxCall(rpc: String): String = "window.web3inbox.chat.postMessage($rpc)"
}

internal class PushProxyInteractor(logger: Logger, webViewWeakReference: WebViewWeakReference) : ProxyInteractor(logger, webViewWeakReference) {
    override fun rpcToWeb3InboxCall(rpc: String): String = "window.web3inbox.push.postMessage($rpc)"
}