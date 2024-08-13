package com.walletconnect.sample.wallet.ui.routes.dialog_routes.session_authenticate

import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.exception.NoConnectivityException
import com.walletconnect.android.utils.cacao.sign
import com.walletconnect.sample.wallet.domain.ACCOUNTS_1_EIP155_ADDRESS
import com.walletconnect.sample.wallet.domain.EthAccountDelegate
import com.walletconnect.sample.wallet.domain.WCDelegate
import com.walletconnect.sample.wallet.ui.common.peer.PeerUI
import com.walletconnect.sample.wallet.ui.common.peer.toPeerUI
import com.walletconnect.util.hexToBytes
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import com.walletconnect.web3.wallet.utils.CacaoSigner

class SessionAuthenticateViewModel : ViewModel() {
    val sessionAuthenticateUI: SessionAuthenticateUI? get() = generateAuthRequestUI()

    fun approve(onSuccess: (String) -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        if (WCDelegate.sessionAuthenticateEvent != null) {
            try {
                val sessionAuthenticate = WCDelegate.sessionAuthenticateEvent!!.first
                val auths = mutableListOf<Wallet.Model.Cacao>()

                val authPayloadParams =
                    Web3Wallet.generateAuthPayloadParams(
                        sessionAuthenticate.payloadParams,
                        supportedChains = listOf("eip155:1", "eip155:137", "eip155:56"),
                        supportedMethods = listOf("personal_sign", "eth_signTypedData", "eth_signTypedData_v4", "eth_sign")
                    )

                authPayloadParams.chains
                    .forEach { chain ->
                        val issuer = "did:pkh:$chain:$ACCOUNTS_1_EIP155_ADDRESS"
                        val message = Web3Wallet.formatAuthMessage(Wallet.Params.FormatAuthMessage(authPayloadParams, issuer))
                        val signature = CacaoSigner.sign(message, EthAccountDelegate.privateKey.hexToBytes(), SignatureType.EIP191)
                        val auth = Web3Wallet.generateAuthObject(authPayloadParams, issuer, signature)
                        auths.add(auth)
                    }

                val approveProposal = Wallet.Params.ApproveSessionAuthenticate(id = sessionAuthenticate.id, auths = auths)
                Web3Wallet.approveSessionAuthenticate(approveProposal,
                    onSuccess = {
                        WCDelegate.sessionAuthenticateEvent = null
                        onSuccess(sessionAuthenticate.participant.metadata?.redirect ?: "")
                    },
                    onError = { error ->
                        if (error.throwable !is NoConnectivityException) {
                            WCDelegate.sessionAuthenticateEvent = null
                        }
                        Firebase.crashlytics.recordException(error.throwable)
                        onError(error.throwable)
                    }
                )
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
                WCDelegate.sessionAuthenticateEvent = null
                onError(e)
            }
        } else {
            onError(Throwable("Authenticate request expired"))
        }
    }

    fun reject(onSuccess: (String) -> Unit = {}, onError: (Throwable) -> Unit = {}) {
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
                        if (error.throwable !is NoConnectivityException) {
                            WCDelegate.sessionAuthenticateEvent = null
                        }
                        Firebase.crashlytics.recordException(error.throwable)
                        onError(error.throwable)
                    })
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
                WCDelegate.sessionAuthenticateEvent = null
                onError(e)
            }
        } else {
            onError(Throwable("Authenticate request expired"))
        }
    }

    private fun generateAuthRequestUI(): SessionAuthenticateUI? {
        return if (WCDelegate.sessionAuthenticateEvent != null) {
            val (sessionAuthenticate, authContext) = WCDelegate.sessionAuthenticateEvent!!
            val messages = mutableListOf<String>()
            sessionAuthenticate.payloadParams.chains
                .forEach { chain ->
                    val issuer = "did:pkh:$chain:$ACCOUNTS_1_EIP155_ADDRESS"
                    val message = try {
                        Web3Wallet.formatAuthMessage(Wallet.Params.FormatAuthMessage(sessionAuthenticate.payloadParams, issuer))
                    } catch (e: Exception) {
                        "Invalid message, error: ${e.message}"
                    }
                    messages.add(message)
                }

            SessionAuthenticateUI(
                peerUI = PeerUI(
                    peerIcon = sessionAuthenticate.participant.metadata?.icons?.firstOrNull().toString(),
                    peerName = sessionAuthenticate.participant.metadata?.name ?: "WalletConnect",
                    peerUri = sessionAuthenticate.participant.metadata?.url ?: "https://walletconnect.com/",
                    peerDescription = sessionAuthenticate.participant.metadata?.url ?: "The communications protocol for web3.",
                    linkMode = sessionAuthenticate.participant.metadata?.linkMode ?: false
                ),
                messages = messages,
                peerContextUI = authContext.toPeerUI()
            )
        } else null
    }
}