package org.walletconnect.walletconnectv2

sealed interface WalletConnectClientListeners {
    fun interface Session : WalletConnectClientListeners {
        fun onSessionProposal(proposal: SessionProposal)
    }
}

//TODO change to proper object once we got session proposal payload from relay server
data class SessionProposal(
    var icon: String = "",
    var name: String = "",
    var uri: String = "",
    var description: String = "",
    var chains: List<String> = emptyList(),
    var methods: List<String> = emptyList()
)