package com.walletconnect.web3.wallet.ui.routes.dialog_routes.auth_request

import android.util.Log
import androidx.lifecycle.ViewModel
import com.walletconnect.android.cacao.sign
import com.walletconnect.sample_common.tag
import com.walletconnect.web3.wallet.domain.ISSUER
import com.walletconnect.web3.wallet.domain.PRIVATE_KEY_1
import com.walletconnect.web3.wallet.domain.WCDelegate
import com.walletconnect.web3.wallet.ui.common.peer.PeerUI
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import com.walletconnect.web3.wallet.utils.CacaoSigner
import com.walletconnect.web3.wallet.utils.SignatureType

class AuthRequestViewModel : ViewModel() {
    val authRequest: AuthRequestUI?
        get() = generateAuthRequestUI(WCDelegate.authRequest)

    fun approve() {
        if (WCDelegate.authRequest != null) {
            val request = requireNotNull(WCDelegate.authRequest)
            val message = Web3Wallet.formatMessage(Wallet.Params.FormatMessage(request.payloadParams, ISSUER)) ?: throw Exception("Error formatting message")

            Web3Wallet.respondAuthRequest(
                Wallet.Params.AuthRequestResponse.Result(
                    id = request.id,
                    signature = CacaoSigner.sign(message, PRIVATE_KEY_1, SignatureType.EIP191),
                    issuer = ISSUER
                )
            ) { error ->
                Log.e(tag(this), error.throwable.stackTraceToString())

                AuthRequestStore.removeActiveSession(request)
            }

            AuthRequestStore.addActiveSession(request)
            WCDelegate.authRequest = null
        }
    }

    fun reject() {
        if (WCDelegate.authRequest != null) {
            val request = requireNotNull(WCDelegate.authRequest)
            //todo: Define Error Codes
            Web3Wallet.respondAuthRequest(
                Wallet.Params.AuthRequestResponse.Error(
                    request.id, 12001, "User Rejected Request"
                )
            ) { error ->
                Log.e(tag(this), error.throwable.stackTraceToString())
            }

            WCDelegate.authRequest = null
        }
    }

    private fun generateAuthRequestUI(authRequest: Wallet.Model.AuthRequest?): AuthRequestUI? {
        return if (authRequest != null) {
            val message = Web3Wallet.formatMessage(Wallet.Params.FormatMessage(authRequest.payloadParams, ISSUER)) ?: throw Exception("Error formatting message")

            AuthRequestUI(
                peerUI = PeerUI(
                    peerIcon = "https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Icon/Gradient/Icon.png",
                    peerName = "WalletConnect",
                    peerUri = "https://walletconnect.com/",
                    peerDescription = "The communications protocol for web3.",
                ),
                message = message
            )
        } else null
    }
}