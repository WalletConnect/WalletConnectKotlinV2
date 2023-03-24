package com.walletconnect.web3.wallet.ui.routes.dialog_routes.session_proposal

import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.sample_common.Chains
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import com.walletconnect.web3.wallet.domain.accounts
import com.walletconnect.web3.wallet.ui.common.peer.PeerUI
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SessionProposalViewModel : ViewModel() {
    val sessionProposal: SessionProposalUI? = generateSessionProposalUI(Web3Wallet.getSessionProposals().last())

    suspend fun approve() {
        return suspendCoroutine { continuation ->
            if (Web3Wallet.getSessionProposals().isNotEmpty()) {
                val sessionProposal: Wallet.Model.SessionProposal = requireNotNull(Web3Wallet.getSessionProposals().last())
                val chains = sessionProposalUI.namespaces.flatMap { (_, proposal) -> proposal.chains!! }
                val selectedAccounts: Map<Chains, String> = chains.mapNotNull { namespaceChainId -> accounts.firstOrNull { (chain, address) -> chain.chainId == namespaceChainId } }.toMap()
                val required: Map<String, Wallet.Model.Namespace.Session> =
                    getSessionNamespacesIndexedByNamespace(selectedAccounts, sessionProposal.requiredNamespaces, chains)
                        .plus(sessionNamespacesIndexedByChain(selectedAccounts, sessionProposal.requiredNamespaces))

                val optional: Map<String, Wallet.Model.Namespace.Session> =
                    getSessionNamespacesIndexedByNamespace(selectedAccounts, sessionProposal.optionalNamespaces, chains)
                        .plus(sessionNamespacesIndexedByChain(selectedAccounts, sessionProposal.optionalNamespaces))
                val sessionNamespaces: Map<String, Wallet.Model.Namespace.Session> = mergeRequiredAndOptional(required, optional)
                val approveProposal = Wallet.Params.SessionApprove(proposerPublicKey = sessionProposal.proposerPublicKey, namespaces = sessionNamespaces)

                Web3Wallet.approveSession(approveProposal,
                    onError = { error ->
                        continuation.resumeWithException(error.throwable)
                        Firebase.crashlytics.recordException(error.throwable)
                    },
                    onSuccess = {
                        continuation.resume(Unit)
                    })
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

    private fun mergeRequiredAndOptional(required: Map<String, Wallet.Model.Namespace.Session>, optional: Map<String, Wallet.Model.Namespace.Session>) =
        (required.asSequence() + optional.asSequence())
            .groupBy({ it.key }, { it.value })
            .mapValues { entry ->
                entry.value.reduce { acc, session ->
                    Wallet.Model.Namespace.Session(
                        acc.chains?.plus(session.chains ?: emptyList()),
                        acc.accounts.plus(session.accounts),
                        acc.methods.plus(session.methods).distinct(),
                        session.events.plus(session.events).distinct()
                    )
                }
            }

    private fun sessionNamespacesIndexedByChain(selectedAccounts: Map<Chains, String>, namespaces: Map<String, Wallet.Model.Namespace.Proposal>) =
        selectedAccounts.filter { (chain: Chains, _) ->
            namespaces
                .filter { (namespaceKey, namespace) -> namespace.chains == null && namespaceKey == chain.chainId }
                .isNotEmpty()
        }.toList()
            .groupBy { (chain: Chains, _: String) -> chain.chainId }
            .asIterable()
            .associate { (key: String, chainData: List<Pair<Chains, String>>) ->
                val accounts = chainData.map { (chain: Chains, accountAddress: String) ->
                    "${chain.chainNamespace}:${chain.chainReference}:${accountAddress}"
                }

                val methods = namespaces.values
                    .filter { namespace -> namespace.chains == null }
                    .flatMap { it.methods }

                val events = namespaces.values
                    .filter { namespace -> namespace.chains == null }
                    .flatMap { it.events }

                key to Wallet.Model.Namespace.Session(
                    accounts = accounts,
                    methods = methods,
                    events = events
                )
            }

    private fun getSessionNamespacesIndexedByNamespace(selectedAccounts: Map<Chains, String>, namespaces: Map<String, Wallet.Model.Namespace.Proposal>, supportedChains: List<String>) =
        selectedAccounts.filter { (chain: Chains, _) ->
            namespaces
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

                val methods = namespaces.values
                    .filter { namespace -> namespace.chains != null }
                    .flatMap { it.methods }

                val events = namespaces.values
                    .filter { namespace -> namespace.chains != null }
                    .flatMap { it.events }

                val chains: List<String> =
                    namespaces.values
                        .filter { namespace -> namespace.chains != null }
                        .flatMap { namespace ->
                            mutableListOf<String>().apply {
                                namespace.chains!!.forEach { chain ->
                                    if (supportedChains.contains(chain)) {
                                        add(chain)
                                    }
                                }
                            }
                        }

                key to Wallet.Model.Namespace.Session(
                    accounts = accounts,
                    methods = methods,
                    events = events,
                    chains = chains.ifEmpty { null })
            }
}