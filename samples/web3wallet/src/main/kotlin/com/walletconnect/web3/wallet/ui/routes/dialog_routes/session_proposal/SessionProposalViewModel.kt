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
    val sessionProposal: SessionProposalUI? = generateSessionProposalUI(Web3Wallet.getSessionProposals().last())

    fun approve() {
        if (Web3Wallet.getSessionProposals().isNotEmpty()) {
            val sessionProposal: Wallet.Model.SessionProposal = requireNotNull(Web3Wallet.getSessionProposals().last())
            val chains = sessionProposalUI.namespaces.flatMap { (namespace, proposal) -> proposal.chains!! }
            val selectedAccounts: Map<Chains, String> = chains.map { namespaceChainId ->
                accounts.firstOrNull { (chain, address) -> chain.chainId == namespaceChainId }
            }.filterNotNull().toMap()

            val sessionNamespacesIndexedByNamespace: Map<String, Wallet.Model.Namespace.Session> =
                selectedAccounts.filter { (chain: Chains, _) ->
                    sessionProposal.requiredNamespaces
                        .filter { (_, namespace) -> namespace.chains != null }
                        .flatMap { (_, namespace) -> namespace.chains!! }
                        .contains(chain.chainId)
                }.toList()
                    .groupBy { (chain: Chains, _: String) -> chain.chainNamespace }
                    .asIterable()
                    .associate { (key: String, chainData: List<Pair<Chains, String>>) ->
                        val accounts = chainData.map { (chain: Chains, accountAddress: String) ->
                            "${chain.chainNamespace}:${chain.chainReference}:${accountAddress}"
                        }

                        val methods = sessionProposal.requiredNamespaces.values
                            .filter { namespace -> namespace.chains != null }
                            .flatMap { it.methods }

                        val events = sessionProposal.requiredNamespaces.values
                            .filter { namespace -> namespace.chains != null }
                            .flatMap { it.events }

                        val chains: List<String> =
                            sessionProposal.requiredNamespaces.values
                                .filter { namespace -> namespace.chains != null }
                                .flatMap { namespace -> namespace.chains!! }

                        key to Wallet.Model.Namespace.Session(
                            accounts = accounts,
                            methods = methods,
                            events = events,
                            chains = chains.ifEmpty { null })
                    }

            val sessionNamespacesIndexedByChain: Map<String, Wallet.Model.Namespace.Session> =
                selectedAccounts.filter { (chain: Chains, _) ->
                    sessionProposal.requiredNamespaces
                        .filter { (namespaceKey, namespace) -> namespace.chains == null && namespaceKey == chain.chainId }
                        .isNotEmpty()
                }.toList()
                    .groupBy { (chain: Chains, _: String) -> chain.chainId }
                    .asIterable()
                    .associate { (key: String, chainData: List<Pair<Chains, String>>) ->
                        val accounts = chainData.map { (chain: Chains, accountAddress: String) ->
                            "${chain.chainNamespace}:${chain.chainReference}:${accountAddress}"
                        }

                        val methods = sessionProposal.requiredNamespaces.values
                            .filter { namespace -> namespace.chains == null }
                            .flatMap { it.methods }

                        val events = sessionProposal.requiredNamespaces.values
                            .filter { namespace -> namespace.chains == null }
                            .flatMap { it.events }

                        key to Wallet.Model.Namespace.Session(
                            accounts = accounts,
                            methods = methods,
                            events = events
                        )
                    }

            val sessionNamespaces = sessionNamespacesIndexedByNamespace.plus(sessionNamespacesIndexedByChain)
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
        Web3Wallet.getSessionProposals().last().let { sessionProposal ->
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