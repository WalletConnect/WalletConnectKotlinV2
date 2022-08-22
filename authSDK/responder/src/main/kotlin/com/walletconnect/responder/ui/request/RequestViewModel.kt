package com.walletconnect.responder.ui.request

import androidx.lifecycle.ViewModel
import com.walletconnect.responder.R

class RequestViewModel : ViewModel() {
    fun fetchRequestProposal(sessionExists: (RequestUI) -> Unit, sessionDNE: () -> Unit) {
        //todo: here goes fetching request
        sessionExists(
            RequestUI(
                "https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Icon/Gradient/Icon.png",
                "WalletConnect",
                "https://walletconnect.com/",
                "The communications protocol for web3.",
                """service.invalid wants you to sign in with your Ethereum account:
0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2

I accept the ServiceOrg Terms of Service: https://service.invalid/tos

URI: https://service.invalid/login
Version: 1
Chain ID: 1
Nonce: 32891756
Issued At: 2021-09-30T16:25:24Z
Resources:
- ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq
- https://example.com/my-web2-claim.json"""
            )
        )
    }

    fun approve() {
        //todo: here goes signing and responding
    }

    fun reject() {
        //todo: here goes responding with error
    }
}