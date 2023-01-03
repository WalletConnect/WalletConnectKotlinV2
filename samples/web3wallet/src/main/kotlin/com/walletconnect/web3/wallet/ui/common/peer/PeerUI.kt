package com.walletconnect.web3.wallet.ui.common.peer


data class PeerUI(
    val peerIcon: String,
    val peerName: String,
    val peerUri: String,
    val peerDescription: String,
){
    companion object {
        val Empty = PeerUI("", "", "", "")
    }
}
