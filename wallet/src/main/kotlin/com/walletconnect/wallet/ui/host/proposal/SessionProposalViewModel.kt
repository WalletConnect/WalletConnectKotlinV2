package com.walletconnect.wallet.ui.host.proposal

import android.util.Log
import androidx.lifecycle.ViewModel
import com.walletconnect.sample_common.EthTestChains
import com.walletconnect.sample_common.tag
import com.walletconnect.wallet.domain.WalletDelegate
import com.walletconnect.wallet.domain.mapOfAllAccounts
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.AuthClient

class SessionProposalViewModel : ViewModel() {

    fun fetchSessionProposal(sessionExists: (SessionProposalUI) -> Unit, sessionDNE: () -> Unit) {
        if (WalletDelegate.sessionProposal != null) {
            val sessionProposalUI = generateSessionProposalEvent(WalletDelegate.sessionProposal!!)
            sessionExists(sessionProposalUI)
        } else {
            sessionDNE()
        }
    }

    fun approve() {
        if (WalletDelegate.sessionProposal != null && WalletDelegate.selectedChainAddressId in mapOfAllAccounts.keys) {
            val selectedAccounts: Map<EthTestChains, String> = mapOfAllAccounts[WalletDelegate.selectedChainAddressId] ?: throw Exception("Can't find account")
            val sessionProposal: WalletConnect.Model.SessionProposal = requireNotNull(WalletDelegate.sessionProposal)
            val sessionNamespaces: Map<String, WalletConnect.Model.Namespace.Session> = selectedAccounts.filter { (chain: EthTestChains, _) ->
                "${chain.chainNamespace}:${chain.chainReference}" in sessionProposal.requiredNamespaces.values.flatMap { it.chains }
            }.toList().groupBy { (chain: EthTestChains, _: String) ->
                chain.chainNamespace
            }.map { (namespaceKey: String, chainData: List<Pair<EthTestChains, String>>) ->
                val accounts = chainData.map { (chain: EthTestChains, accountAddress: String) ->
                    "${chain.chainNamespace}:${chain.chainReference}:${accountAddress}"
                }
                val methods = sessionProposal.requiredNamespaces.values.flatMap { it.methods }
                val events = sessionProposal.requiredNamespaces.values.flatMap { it.events }

                namespaceKey to WalletConnect.Model.Namespace.Session(accounts = accounts, methods = methods, events = events, extensions = null)
            }.toMap()

            val approveProposal = WalletConnect.Params.Approve(
                proposerPublicKey = sessionProposal.proposerPublicKey,
                namespaces = sessionNamespaces
            )

            AuthClient.approveSession(approveProposal) { error ->
                Log.e(tag(this@SessionProposalViewModel), error.throwable.stackTraceToString())
            }

            WalletDelegate.clearCache()
        }
    }

    fun reject() {
        WalletDelegate.sessionProposal?.let { sessionProposal ->
            val rejectionReason = "Reject Session"
            val reject = WalletConnect.Params.Reject(
                proposerPublicKey = sessionProposal.proposerPublicKey,
                reason = rejectionReason,
                code = 406
            )

            AuthClient.rejectSession(reject) { error ->
                Log.d(tag(this@SessionProposalViewModel), "sending reject error: $error")
            }

            WalletDelegate.clearCache()
        }
    }

    private fun generateSessionProposalEvent(sessionProposal: WalletConnect.Model.SessionProposal): SessionProposalUI {
        return SessionProposalUI(
            peerIcon = sessionProposal.icons.first().toString(),
            peerName = sessionProposal.name,
            proposalUri = sessionProposal.url,
            peerDescription = sessionProposal.description,
            chains = sessionProposal.requiredNamespaces.flatMap { it.value.chains }.joinToString("\n"),
            methods = sessionProposal.requiredNamespaces.flatMap { it.value.methods }.joinToString("\n")
        )
    }
}