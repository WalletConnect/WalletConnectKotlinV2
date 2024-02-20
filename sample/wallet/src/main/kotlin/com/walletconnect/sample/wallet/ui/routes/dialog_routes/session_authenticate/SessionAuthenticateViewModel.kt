package com.walletconnect.sample.wallet.ui.routes.dialog_routes.session_authenticate

import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.utils.cacao.signHex
import com.walletconnect.sample.wallet.domain.ACCOUNTS_1_EIP155_ADDRESS
import com.walletconnect.sample.wallet.domain.EthAccountDelegate.privateKey
import com.walletconnect.sample.wallet.domain.WCDelegate
import com.walletconnect.sample.wallet.ui.common.peer.PeerUI
import com.walletconnect.sample.wallet.ui.common.peer.toPeerUI
import com.walletconnect.util.hexToBytes
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet.generateAuthObject
import com.walletconnect.web3.wallet.client.Web3Wallet.generateAuthPayloadParams
import com.walletconnect.web3.wallet.utils.CacaoSigner
import org.web3j.utils.Numeric

class SessionAuthenticateViewModel : ViewModel() {
    val sessionAuthenticateUI: SessionAuthenticateUI? get() = generateAuthRequestUI()

    fun approve(onSuccess: (String) -> Unit = {}, onError: (String) -> Unit = {}) {
        if (WCDelegate.sessionAuthenticateEvent != null) {
            try {
                val sessionAuthenticate = WCDelegate.sessionAuthenticateEvent!!.first
                val auths = mutableListOf<Wallet.Model.Cacao>()

                sessionAuthenticateUI?.messages?.forEach { issuerToMessage ->
                    val messageToSign = Numeric.toHexString(issuerToMessage.second.toByteArray())
                    val signature = CacaoSigner.signHex(messageToSign, privateKey.hexToBytes(), SignatureType.EIP191)
                    val authPayloadParams =
                        generateAuthPayloadParams(
                            sessionAuthenticate.payloadParams,
                            supportedChains = listOf("eip155:1", "eip155:137", "eip155:56"),
                            supportedMethods = listOf("personal_sign", "eth_signTypedData", "eth_sign")
                        )
                    val auth = generateAuthObject(authPayloadParams, issuerToMessage.first, signature)
                    auths.add(auth)
                }

                val approveProposal = Wallet.Params.ApproveSessionAuthenticate(id = sessionAuthenticate.id, auths = auths)
                Web3Wallet.approveSessionAuthenticate(approveProposal,
                    onError = { error ->
                        Firebase.crashlytics.recordException(error.throwable)
                        WCDelegate.sessionAuthenticateEvent = null
                        onError(error.throwable.message ?: "Undefined error, please check your Internet connection")
                    },
                    onSuccess = {
                        WCDelegate.sessionAuthenticateEvent = null
                        onSuccess(sessionAuthenticate.participant.metadata?.redirect ?: "")
                    })
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
                WCDelegate.sessionProposalEvent = null
                onError(e.message ?: "Undefined error, please check your Internet connection")
            }
        } else {
            onError("Authenticate request expired")
        }
    }

    fun reject(onSuccess: (String) -> Unit = {}, onError: (String) -> Unit = {}) {
        if (WCDelegate.sessionAuthenticateEvent != null) {
            try {
                val sessionAuthenticate = WCDelegate.sessionAuthenticateEvent!!.first
                val rejectionReason = "Reject Session Authenticate"
                val reject = Wallet.Params.RejectSessionAuthenticate(
                    id = sessionAuthenticate.id,
                    reason = rejectionReason
                )

                Web3Wallet.rejectSessionAuthenticate(reject,
                    onSuccess = {
                        WCDelegate.sessionAuthenticateEvent = null
                        onSuccess(sessionAuthenticate.participant.metadata?.redirect ?: "")
                    },
                    onError = { error ->
                        Firebase.crashlytics.recordException(error.throwable)
                        WCDelegate.sessionAuthenticateEvent = null
                        onError(error.throwable.message ?: "Undefined error, please check your Internet connection")
                    })
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
                WCDelegate.sessionAuthenticateEvent = null
                onError(e.message ?: "Undefined error, please check your Internet connection")
            }
        } else {
            onError("Authenticate request expired")
        }
    }

    private fun generateAuthRequestUI(): SessionAuthenticateUI? {
        return if (WCDelegate.sessionAuthenticateEvent != null) {
            val (sessionAuthenticate, authContext) = WCDelegate.sessionAuthenticateEvent!!
            val issuerToMessages = mutableListOf<Pair<String, String>>()
            sessionAuthenticate.payloadParams.chains
                .filter { chain -> chain == "eip155:1" || chain == "eip155:137" || chain == "eip155:56" }
                .forEach { chain ->
                    val issuer = "did:pkh:$chain:$ACCOUNTS_1_EIP155_ADDRESS"
                    val authPayloadParams =
                        generateAuthPayloadParams(
                            sessionAuthenticate.payloadParams,
                            supportedChains = listOf("eip155:1", "eip155:137", "eip155:56"),
                            supportedMethods = listOf("personal_sign", "eth_signTypedData", "eth_sign")
                        )
                    val message = Web3Wallet.formatAuthMessage(Wallet.Params.FormatAuthMessage(authPayloadParams, issuer)) ?: throw Exception("Invalid message")
                    issuerToMessages.add(issuer to message)
                }

            SessionAuthenticateUI(
                peerUI = PeerUI(
                    peerIcon = "https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Icon/Gradient/Icon.png",
                    peerName = "Kotlin Wallet",
                    peerUri = "https://walletconnect.com/",
                    peerDescription = "The communications protocol for web3.",
                ),
                messages = issuerToMessages,
                peerContextUI = authContext.toPeerUI()
            )
        } else null
    }
}