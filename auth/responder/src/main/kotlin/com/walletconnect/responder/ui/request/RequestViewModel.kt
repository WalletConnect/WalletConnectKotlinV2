package com.walletconnect.responder.ui.request

import android.util.Log
import androidx.lifecycle.ViewModel
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.AuthClient
import com.walletconnect.auth.signature.CacaoType
import com.walletconnect.auth.signature.SignatureType
import com.walletconnect.auth.signature.cacao.CacaoSigner
import com.walletconnect.responder.domain.PRIVATE_KEY_1
import com.walletconnect.sample_common.tag

class RequestViewModel : ViewModel() {

    fun fetchRequestProposal(sessionExists: (RequestUI) -> Unit, sessionDNE: () -> Unit) {
        //todo: here goes fetching request
        if (RequestStore.currentRequest != null) {
            sessionExists(
                //todo: How to get Requester Metadata here?
                RequestUI(
                    peerIcon = "https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Icon/Gradient/Icon.png",
                    peerName = "WalletConnect",
                    proposalUri = "https://walletconnect.com/",
                    peerDescription = "The communications protocol for web3.",
                    message = RequestStore.currentRequest!!.message
                )
            )
        } else {
            sessionDNE()
        }
    }

    fun approve() {
        val request = RequestStore.currentRequest!!
        AuthClient.respond(Auth.Params.Respond.Result(request.id, CacaoSigner.sign(request.message, PRIVATE_KEY_1, SignatureType.EIP191))) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }
    }

    fun reject() {
        val request = RequestStore.currentRequest!!
        //todo: Define Error Codes
        AuthClient.respond(
            Auth.Params.Respond.Error(request.id, 12001, "User Rejected Request")
        ) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }
    }
}