@file:JvmSynthetic

package com.walletconnect.web3.inbox.proxy

import android.webkit.JavascriptInterface
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.json_rpc.Web3InboxSerializer
import com.walletconnect.web3.inbox.proxy.request.*


internal class ProxyRequestHandler(
    private val logger: Logger,
    private val registerRequestUseCase: RegisterRequestUseCase,
    private val getReceivedInvitesRequestUseCase: GetReceivedInvitesRequestUseCase,
    private val getSentInvitesRequestUseCase: GetSentInvitesRequestUseCase,
    private val getThreadsRequestUseCase: GetThreadsRequestUseCase,
    private val getMessagesRequestUseCase: GetMessagesRequestUseCase,
    private val acceptRequestUseCase: AcceptRequestUseCase,
    private val rejectRequestUseCase: RejectRequestUseCase,
    private val resolveRequestUseCase: ResolveRequestUseCase,
    private val messageRequestUseCase: MessageRequestUseCase,
    private val inviteRequestUseCase: InviteRequestUseCase,
) {

    @JavascriptInterface
    fun postMessage(rpc: String) {
        logger.log("postMessage: $rpc")
        val rpc = Web3InboxSerializer.deserializeRpc(rpc) ?: return logger.error("Unable to deserialize: $rpc")
        if (rpc !is Web3InboxRPC.Request) return logger.error("Not a request: $rpc")
        when (val params = rpc.params) {
            is Web3InboxParams.Request.RegisterParams -> registerRequestUseCase(rpc, params)
            is Web3InboxParams.Request.GetReceivedInvitesParams -> getReceivedInvitesRequestUseCase(rpc, params)
            is Web3InboxParams.Request.GetSentInvitesParams -> getSentInvitesRequestUseCase(rpc, params)
            is Web3InboxParams.Request.GetThreadsParams -> getThreadsRequestUseCase(rpc, params)
            is Web3InboxParams.Request.AcceptParams -> acceptRequestUseCase(rpc, params)
            is Web3InboxParams.Request.RejectParams -> rejectRequestUseCase(rpc, params)
            is Web3InboxParams.Request.ResolveParams -> resolveRequestUseCase(rpc, params)
            is Web3InboxParams.Request.GetMessagesParams -> getMessagesRequestUseCase(rpc, params)
            is Web3InboxParams.Request.MessageParams -> messageRequestUseCase(rpc, params)
            is Web3InboxParams.Request.InviteParams -> inviteRequestUseCase(rpc, params)
        }
    }
}