package com.walletconnect.web3.wallet.ui.routes.dialog_routes.session_request

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.sample_common.Chains
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import com.walletconnect.web3.wallet.domain.WCDelegate
import com.walletconnect.web3.wallet.ui.common.peer.PeerUI

class SessionRequestViewModel : ViewModel() {
    var sessionRequest: SessionRequestUI = generateSessionRequestUI(WCDelegate.sessionRequest)

    private fun clearSessionRequest() {
        WCDelegate.sessionRequest = null
        sessionRequest = SessionRequestUI.Initial
    }

    fun reject(sendSessionRequestResponseDeepLink: (Uri) -> Unit) {
        val sessionRequest = sessionRequest as? SessionRequestUI.Content
        if (sessionRequest != null) {
            val result = Wallet.Params.SessionRequestResponse(
                sessionTopic = sessionRequest.topic,
                jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcError(
                    id = sessionRequest.requestId,
                    code = 500,
                    message = "Kotlin Wallet Error"
                )
            )

            Web3Wallet.respondSessionRequest(result) { error ->
                Firebase.crashlytics.recordException(error.throwable)
            }

            sendResponseDeepLink(sessionRequest, sendSessionRequestResponseDeepLink)
            clearSessionRequest()
        }
    }

    fun approve(sendSessionRequestResponseDeepLink: (Uri) -> Unit) {
        val sessionRequest = sessionRequest as? SessionRequestUI.Content
        if (sessionRequest != null) {
            val result: String = when {
                //TODO: calculate proper values
                sessionRequest.chain?.contains(
                    Chains.Info.Eth.chain,
                    true
                ) == true -> """0xa3f20717a250c2b0b729b7e5becbff67fdaef7e0699da4de7ca5895b02a170a12d887fd3b17bfdce3481f10bea41f45ba9f709d39ce8325427b57afcfc994cee1b"""
                sessionRequest.chain?.contains(
                    Chains.Info.Cosmos.chain,
                    true
                ) == true -> """{"signature":"pBvp1bMiX6GiWmfYmkFmfcZdekJc19GbZQanqaGa\/kLPWjoYjaJWYttvm17WoDMyn4oROas4JLu5oKQVRIj911==","pub_key":{"value":"psclI0DNfWq6cOlGrKD9wNXPxbUsng6Fei77XjwdkPSt","type":"tendermint\/PubKeySecp256k1"}}"""
                else -> throw Exception("Unsupported Chain")
            }
            val response = Wallet.Params.SessionRequestResponse(
                sessionTopic = sessionRequest.topic,
                jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcResult(
                    sessionRequest.requestId,
                    result
                )
            )

            Web3Wallet.respondSessionRequest(response) { error ->
                Firebase.crashlytics.recordException(error.throwable)
            }
            sendResponseDeepLink(sessionRequest, sendSessionRequestResponseDeepLink)
            clearSessionRequest()
        }
    }

    private fun sendResponseDeepLink(
        sessionRequest: SessionRequestUI.Content,
        sendSessionRequestResponseDeepLink: (Uri) -> Unit,
    ) {
        Web3Wallet.getActiveSessionByTopic(sessionRequest.topic)?.redirect?.toUri()
            ?.let { deepLinkUri -> sendSessionRequestResponseDeepLink(deepLinkUri) }
    }

    private fun generateSessionRequestUI(sessionRequest: Wallet.Model.SessionRequest?): SessionRequestUI {
        return if (sessionRequest != null) {
            SessionRequestUI.Content(
                peerUI = PeerUI(
                    peerName = sessionRequest.peerMetaData?.name ?: "",
                    peerIcon = sessionRequest.peerMetaData?.icons?.firstOrNull() ?: "",
                    peerUri = sessionRequest.peerMetaData?.url ?: "",
                    peerDescription = sessionRequest.peerMetaData?.description ?: "",
                ),
                topic = sessionRequest.topic,
                requestId = sessionRequest.request.id,
                param = sessionRequest.request.params,
                chain = sessionRequest.chainId,
                method = sessionRequest.request.method
            )
        } else SessionRequestUI.Initial
    }
}