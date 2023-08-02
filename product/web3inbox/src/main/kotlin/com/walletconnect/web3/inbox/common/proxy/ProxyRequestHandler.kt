@file:JvmSynthetic

package com.walletconnect.web3.inbox.common.proxy

import android.webkit.JavascriptInterface
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.inbox.chat.request.ChatProxyRequestHandler
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.json_rpc.Web3InboxSerializer
import com.walletconnect.web3.inbox.push.request.PushProxyRequestHandler


internal class ProxyRequestHandler(
    private val logger: Logger,
    private val pushProxyRequestHandler: PushProxyRequestHandler,
    private val chatProxyRequestHandler: ChatProxyRequestHandler,
) {

    @JavascriptInterface
    fun postMessage(rpcAsString: String) {
        logger.log("Incoming request from W3I: $rpcAsString")
        val safeRpc = rpcAsString.ensureParamsAreIncluded()
        val rpc = Web3InboxSerializer.deserializeRpc(safeRpc) ?: return logger.error("Unable to deserialize: $safeRpc")
        if (rpc !is Web3InboxRPC.Request) return logger.error("Not a request: $rpc")
        when (rpc) {
            is Web3InboxRPC.Request.Chat -> chatProxyRequestHandler.handleRequest(rpc)
            is Web3InboxRPC.Request.Notify -> pushProxyRequestHandler.handleRequest(rpc)
        }
    }

    private fun String.ensureParamsAreIncluded(): String =
        if (!this.contains("params")) this.removeSuffix("}") + ",\"params\":{}}"
        else this
}