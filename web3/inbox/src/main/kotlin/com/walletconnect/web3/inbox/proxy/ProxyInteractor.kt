package com.walletconnect.web3.inbox.proxy

import android.os.Handler
import android.os.Looper
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.inbox.common.exception.WebViewIsNullException
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.json_rpc.Web3InboxSerializer
import com.walletconnect.web3.inbox.webview.WebViewWeakReference

internal class ProxyInteractor(
    private val logger: Logger,
    private val webViewWeakReference: WebViewWeakReference
) {
    @Throws(WebViewIsNullException::class)
    fun <T : JsonRpcResponse> respond(rpc: T) {
        Handler(Looper.getMainLooper()).post {
            val rpcAsString = Web3InboxSerializer.serializeRpc(rpc) ?: return@post logger.error("Unable to serialize: $rpc")
            logger.log("Responding: $rpcAsString")
            val script = rpcAsString.asWeb3InboxCall()
            webViewWeakReference.webView.evaluateJavascript(script, null)
        }
    }

    @Throws(WebViewIsNullException::class)
    fun <T : Web3InboxRPC.Call> call(rpc: T) {
        Handler(Looper.getMainLooper()).post {
            val rpcAsString = Web3InboxSerializer.serializeRpc(rpc) ?: return@post logger.error("Unable to serialize: $rpc")
            logger.log("Calling: $rpcAsString")
            val script = rpcAsString.asWeb3InboxCall()
            webViewWeakReference.webView.evaluateJavascript(script, null)
        }
    }

    private fun String.asWeb3InboxCall(): String = "window.web3inbox.chat.postMessage($this)"
}