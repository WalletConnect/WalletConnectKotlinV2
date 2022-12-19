package com.walletconnect.showcase.ui.routes.dialog_routes.session_proposal

import android.util.Log
import androidx.lifecycle.ViewModel
import com.walletconnect.sample_common.Chains
import com.walletconnect.sample_common.tag
import com.walletconnect.showcase.domain.WCDelegate
import com.walletconnect.showcase.domain.accounts
import com.walletconnect.showcase.ui.common.peer.PeerUI
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet

class SessionProposalViewModel : ViewModel() {
    val sessionProposal: SessionProposalUI? = generateSessionProposalUI(WCDelegate.sessionProposal)

    fun approve() {
        if (WCDelegate.sessionProposal != null) {

            val sessionProposal: Wallet.Model.SessionProposal = requireNotNull(WCDelegate.sessionProposal)
            val chains = sessionProposalUI.namespaces.flatMap { (namespace, proposal) -> proposal.chains }

            val selectedAccounts: Map<Chains, String> = chains.map { namespaceChainId ->
                accounts.firstOrNull { (chain, address) -> chain.chainId == namespaceChainId }
            }.filterNotNull().toMap()

            val sessionNamespaces: Map<String, Wallet.Model.Namespace.Session> = selectedAccounts.filter { (chain: Chains, _) ->
                "${chain.chainNamespace}:${chain.chainReference}" in sessionProposal.requiredNamespaces.values.flatMap { it.chains }
            }.toList().groupBy { (chain: Chains, _: String) ->
                chain.chainNamespace
            }.map { (namespaceKey: String, chainData: List<Pair<Chains, String>>) ->
                val accounts = chainData.filter { (chain: Chains, _) ->
                    chain.chainNamespace == namespaceKey
                }.map { (chain: Chains, accountAddress: String) ->
                    "${chain.chainNamespace}:${chain.chainReference}:${accountAddress}"
                }
                val methods = sessionProposal.requiredNamespaces[namespaceKey]?.methods ?: emptyList()
                val events = sessionProposal.requiredNamespaces[namespaceKey]?.events ?: emptyList()

                namespaceKey to Wallet.Model.Namespace.Session(accounts = accounts, methods = methods, events = events, extensions = null)
            }.toMap()

            val approveProposal = Wallet.Params.SessionApprove(
                proposerPublicKey = sessionProposal.proposerPublicKey,
                namespaces = sessionNamespaces
            )


            Web3Wallet.approveSession(approveProposal) { error ->
                Log.e(tag(this@SessionProposalViewModel), error.throwable.stackTraceToString())
            }
        }
    }

    fun reject() {
        WCDelegate.sessionProposal?.let { sessionProposal ->
            val rejectionReason = "Reject Session"
            val reject = Wallet.Params.SessionReject(
                proposerPublicKey = sessionProposal.proposerPublicKey,
                reason = rejectionReason
            )

            Web3Wallet.rejectSession(reject) { error ->
                Log.d(tag(this@SessionProposalViewModel), "sending reject error: $error")
            }
        }
    }

    private fun generateSessionProposalUI(sessionProposal: Wallet.Model.SessionProposal?): SessionProposalUI? {
        return if (sessionProposal != null) {
            SessionProposalUI(
                peerUI = PeerUI(
                    peerIcon = sessionProposal.icons.firstOrNull().toString(),
                    peerName = sessionProposal.name,
                    peerDescription = sessionProposal.description,
                    peerUri = sessionProposal.url,
                ),
                namespaces = sessionProposal.requiredNamespaces
            )
        } else null
    }
}