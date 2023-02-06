package com.walletconnect.web3.wallet.ui.routes.dialog_routes.session_proposal

import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.sample_common.Chains
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import com.walletconnect.web3.wallet.domain.WCDelegate
import com.walletconnect.web3.wallet.domain.accounts
import com.walletconnect.web3.wallet.ui.common.peer.PeerUI

class SessionProposalViewModel : ViewModel() {
    val sessionProposal: SessionProposalUI? = generateSessionProposalUI(WCDelegate.sessionProposal)

    fun approve() {
        if (WCDelegate.sessionProposal != null) {
            val sessionProposal: Wallet.Model.SessionProposal = requireNotNull(WCDelegate.sessionProposal)
            val chains = sessionProposalUI.namespaces.flatMap { (namespaceKey, proposal) ->
                if (proposal.chains != null) {
                    proposal.chains!!
                } else {
                    listOf(namespaceKey)
                }
            }

            val selectedAccounts: Map<Chains, String> =
                chains.mapNotNull { namespaceChainId -> accounts.firstOrNull { (chain, address) -> chain.chainId == namespaceChainId } }.toMap()

            val sessionNamespaces: Map<String, Wallet.Model.Namespace.Session> = selectedAccounts.filter { (chain: Chains, _) ->
                "${chain.chainNamespace}:${chain.chainReference}" in sessionProposal.requiredNamespaces.flatMap { (namespaceKey, namespace) ->
                    if (namespace.chains != null) {
                        namespace.chains!!
                    } else {
                        listOf(namespaceKey)
                    }
                }

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

                namespaceKey to Wallet.Model.Namespace.Session(accounts = accounts, methods = methods, events = events)
            }.toMap()

            val approveProposal = Wallet.Params.SessionApprove(
                proposerPublicKey = sessionProposal.proposerPublicKey,
                namespaces = sessionNamespaces
            )


            Web3Wallet.approveSession(approveProposal) { error ->
                Firebase.crashlytics.recordException(error.throwable)
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
                Firebase.crashlytics.recordException(error.throwable)
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