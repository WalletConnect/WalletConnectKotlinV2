package com.walletconnect.sample.wallet.ui.routes.dialog_routes.auth_request

import android.util.Log
import androidx.lifecycle.ViewModel
import com.walletconnect.android.utils.cacao.signHex
import com.walletconnect.sample.wallet.domain.ISSUER
import com.walletconnect.sample.wallet.domain.PRIVATE_KEY_1
import com.walletconnect.sample.wallet.domain.WCDelegate
import com.walletconnect.sample.wallet.ui.common.peer.PeerUI
import com.walletconnect.sample.wallet.ui.common.peer.toPeerUI
import com.walletconnect.sample_common.tag
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import com.walletconnect.web3.wallet.utils.CacaoSigner
import com.walletconnect.web3.wallet.utils.SignatureType
import org.web3j.utils.Numeric.toHexString

class AuthRequestViewModel : ViewModel() {
    val authRequest: AuthRequestUI?
        get() = generateAuthRequestUI()

    fun approve() {
        if (WCDelegate.authRequestEvent != null) {
            val request = requireNotNull(WCDelegate.authRequestEvent!!.first)
            val message = Web3Wallet.formatMessage(Wallet.Params.FormatMessage(request.payloadParams, ISSUER)) ?: throw Exception("Error formatting message")

            Web3Wallet.respondAuthRequest(
                Wallet.Params.AuthRequestResponse.Result(
                    id = request.id,
                    signature = CacaoSigner.signHex(toHexString(message.toByteArray()), PRIVATE_KEY_1, SignatureType.EIP191),
                    issuer = ISSUER
                )
            ) { error ->
                Log.e(tag(this), error.throwable.stackTraceToString())

                AuthRequestStore.removeActiveSession(request)
            }

            AuthRequestStore.addActiveSession(request)
            WCDelegate.authRequestEvent = null
        }
    }

    fun reject() {
        if (WCDelegate.authRequestEvent != null) {
            val request = requireNotNull(WCDelegate.authRequestEvent!!.first)
            //todo: Define Error Codes
            Web3Wallet.respondAuthRequest(
                Wallet.Params.AuthRequestResponse.Error(
                    request.id, 12001, "User Rejected Request"
                )
            ) { error ->
                Log.e(tag(this), error.throwable.stackTraceToString())
            }

            WCDelegate.authRequestEvent = null
        }
    }

    private fun generateAuthRequestUI(): AuthRequestUI? {
        return if (WCDelegate.authRequestEvent != null) {
            val (authRequest, authContext) = WCDelegate.authRequestEvent!!
            val message = Web3Wallet.formatMessage(Wallet.Params.FormatMessage(authRequest.payloadParams, ISSUER)) ?: throw Exception("Error formatting message")

            AuthRequestUI(
                peerUI = PeerUI(
                    peerIcon = "https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Icon/Gradient/Icon.png",
                    peerName = "WalletConnect",
                    peerUri = "https://walletconnect.com/",
                    peerDescription = "The communications protocol for web3.",
                ),
                message = message,
                peerContextUI = authContext.toPeerUI()
            )
        } else null
    }
}