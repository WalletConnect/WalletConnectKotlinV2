package com.walletconnect.wallet.ui.host.proposal

import android.util.Log
import androidx.lifecycle.ViewModel
import com.walletconnect.sample_common.Chains
import com.walletconnect.sample_common.tag
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.wallet.domain.WalletDelegate
import com.walletconnect.wallet.domain.mapOfAllAccounts

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
            val selectedAccounts: Map<Chains, String> =
                mapOfAllAccounts[WalletDelegate.selectedChainAddressId] ?: throw Exception("Can't find account")
            val sessionProposal: Sign.Model.SessionProposal = requireNotNull(WalletDelegate.sessionProposal)

            val sessionNamespacesIndexedByNamespace: Map<String, Sign.Model.Namespace.Session> =
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

                        key to Sign.Model.Namespace.Session(
                            accounts = accounts,
                            methods = methods,
                            events = events,
                            chains = chains.ifEmpty { null })
                    }

            val sessionNamespacesIndexedByChain: Map<String, Sign.Model.Namespace.Session> =
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

                        key to Sign.Model.Namespace.Session(
                            accounts = accounts,
                            methods = methods,
                            events = events
                        )
                    }

            val sessionNamespaces = sessionNamespacesIndexedByNamespace.plus(sessionNamespacesIndexedByChain)
            val approveProposal = Sign.Params.Approve(
                proposerPublicKey = sessionProposal.proposerPublicKey,
                namespaces = sessionNamespaces
            )

            SignClient.approveSession(approveProposal) { error ->
                Log.e(tag(this@SessionProposalViewModel), error.throwable.stackTraceToString())
            }

            WalletDelegate.clearCache()
        }
    }

    fun reject() {
        WalletDelegate.sessionProposal?.let { sessionProposal ->
            val rejectionReason = "Reject Session"
            val reject = Sign.Params.Reject(
                proposerPublicKey = sessionProposal.proposerPublicKey,
                reason = rejectionReason
            )

            SignClient.rejectSession(reject) { error ->
                Log.d(tag(this@SessionProposalViewModel), "sending reject error: $error")
            }

            WalletDelegate.clearCache()
        }
    }

    private fun generateSessionProposalEvent(sessionProposal: Sign.Model.SessionProposal): SessionProposalUI {
        return SessionProposalUI(
            peerIcon = sessionProposal.icons.firstOrNull().toString(),
            peerName = sessionProposal.name,
            proposalUri = sessionProposal.url,
            peerDescription = sessionProposal.description,
            chains = sessionProposal.requiredNamespaces.flatMap { (namespaceKey, namespace) ->
                if (namespace.chains != null) {
                    namespace.chains!!
                } else {
                    listOf(namespaceKey)
                }
            }.joinToString("\n"),
            methods = sessionProposal.requiredNamespaces.flatMap { it.value.methods }.distinct().joinToString("\n"),
            events = sessionProposal.requiredNamespaces.flatMap { it.value.events.distinct() }.distinct().joinToString("\n")
        )
    }
}