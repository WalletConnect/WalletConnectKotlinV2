package com.walletconnect.sample.wallet.ui.routes.dialog_routes.session_request

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.exception.NoConnectivityException
import com.walletconnect.android.utils.cacao.sign
import com.walletconnect.sample.common.Chains
import com.walletconnect.sample.wallet.domain.EthAccountDelegate
import com.walletconnect.sample.wallet.domain.WCDelegate
import com.walletconnect.sample.wallet.ui.common.peer.PeerUI
import com.walletconnect.sample.wallet.ui.common.peer.toPeerUI
import com.walletconnect.util.hexToBytes
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import com.walletconnect.web3.wallet.utils.CacaoSigner
import org.json.JSONArray
import org.web3j.utils.Numeric.hexStringToByteArray

class SessionRequestViewModel : ViewModel() {
    var sessionRequestUI: SessionRequestUI = generateSessionRequestUI()

    private fun clearSessionRequest() {
        WCDelegate.sessionRequestEvent = null
        WCDelegate.currentId = null
        sessionRequestUI = SessionRequestUI.Initial
    }

    fun reject(onSuccess: (Uri?) -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        try {
            val sessionRequest = sessionRequestUI as? SessionRequestUI.Content
            if (sessionRequest != null) {
                val result = Wallet.Params.SessionRequestResponse(
                    sessionTopic = sessionRequest.topic,
                    jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcError(
                        id = sessionRequest.requestId,
                        code = 500,
                        message = "Kotlin Wallet Error"
                    )
                )
                val redirect = Web3Wallet.getActiveSessionByTopic(sessionRequest.topic)?.redirect?.toUri()
                Web3Wallet.respondSessionRequest(result,
                    onSuccess = {
                        clearSessionRequest()
                        onSuccess(redirect)
                    },
                    onError = { error ->
                        Firebase.crashlytics.recordException(error.throwable)
                        if (error.throwable !is NoConnectivityException) {
                            clearSessionRequest()
                        }
                        onError(error.throwable)
                    })
            } else {
                onError(Throwable("Reject - Cannot find session request"))
            }
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
            clearSessionRequest()
            onError(e.cause ?: Throwable("Undefined error, please check your Internet connection"))
        }
    }

    private fun extractMessageParamFromPersonalSign(input: String): String {
        val jsonArray = JSONArray(input)
        return if (jsonArray.length() > 0) {
            if (jsonArray.getString(0).startsWith("0x")) {
                String(hexStringToByteArray(jsonArray.getString(0)))
            } else {
                jsonArray.getString(0)
            }
        } else {
            throw IllegalArgumentException()
        }
    }

    fun approve(onSuccess: (Uri?) -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        try {
            val sessionRequest = sessionRequestUI as? SessionRequestUI.Content
            if (sessionRequest != null) {
                val result: String = when {
                    sessionRequest.method == PERSONAL_SIGN_METHOD -> CacaoSigner.sign(
                        sessionRequest.param,
                        EthAccountDelegate.privateKey.hexToBytes(),
                        SignatureType.EIP191
                    ).s

                    sessionRequest.chain?.contains(Chains.Info.Eth.chain, true) == true ->
                        """0xa3f20717a250c2b0b729b7e5becbff67fdaef7e0699da4de7ca5895b02a170a12d887fd3b17bfdce3481f10bea41f45ba9f709d39ce8325427b57afcfc994cee1b"""

                    sessionRequest.chain?.contains(Chains.Info.Cosmos.chain, true) == true ->
                        """{"signature":"pBvp1bMiX6GiWmfYmkFmfcZdekJc19GbZQanqaGa\/kLPWjoYjaJWYttvm17WoDMyn4oROas4JLu5oKQVRIj911==","pub_key":{"value":"psclI0DNfWq6cOlGrKD9wNXPxbUsng6Fei77XjwdkPSt","type":"tendermint\/PubKeySecp256k1"}}"""
                    //Note: Only for testing purposes - it will always fail on Dapp side
                    sessionRequest.chain?.contains(Chains.Info.Solana.chain, true) == true ->
                        """{"signature":"pBvp1bMiX6GiWmfYmkFmfcZdekJc19GbZQanqaGa\/kLPWjoYjaJWYttvm17WoDMyn4oROas4JLu5oKQVRIj911==","pub_key":{"value":"psclI0DNfWq6cOlGrKD9wNXPxbUsng6Fei77XjwdkPSt","type":"tendermint\/PubKeySecp256k1"}}"""

                    else -> throw Exception("Unsupported Chain")
                }
                val response = Wallet.Params.SessionRequestResponse(
                    sessionTopic = sessionRequest.topic,
                    jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcResult(
                        sessionRequest.requestId,
                        result
                    )
                )

                val redirect = Web3Wallet.getActiveSessionByTopic(sessionRequest.topic)?.redirect?.toUri()
                Web3Wallet.respondSessionRequest(response,
                    onSuccess = {
                        clearSessionRequest()
                        onSuccess(redirect)
                    },
                    onError = { error ->
                        Firebase.crashlytics.recordException(error.throwable)
                        if (error.throwable !is NoConnectivityException) {
                            clearSessionRequest()
                        }
                        onError(error.throwable)
                    })
            } else {
                onError(Throwable("Approve - Cannot find session request"))
            }
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
            clearSessionRequest()
            onError(e.cause ?: Throwable("Undefined error, please check your Internet connection"))
        }
    }

    private fun generateSessionRequestUI(): SessionRequestUI {
        return if (WCDelegate.sessionRequestEvent != null) {
            val (sessionRequest, context) = WCDelegate.sessionRequestEvent!!
            SessionRequestUI.Content(
                peerUI = PeerUI(
                    peerName = sessionRequest.peerMetaData?.name ?: "",
                    peerIcon = sessionRequest.peerMetaData?.icons?.firstOrNull() ?: "",
                    peerUri = sessionRequest.peerMetaData?.url ?: "",
                    peerDescription = sessionRequest.peerMetaData?.description ?: "",
                    linkMode = sessionRequest.peerMetaData?.linkMode ?: false
                ),
                topic = sessionRequest.topic,
                requestId = sessionRequest.request.id,
                param = if (sessionRequest.request.method == PERSONAL_SIGN_METHOD) extractMessageParamFromPersonalSign(sessionRequest.request.params) else sessionRequest.request.params,
                chain = sessionRequest.chainId,
                method = sessionRequest.request.method,
                peerContextUI = context.toPeerUI()
            )
        } else {
            SessionRequestUI.Initial
        }
    }
}

private const val PERSONAL_SIGN_METHOD = "personal_sign"