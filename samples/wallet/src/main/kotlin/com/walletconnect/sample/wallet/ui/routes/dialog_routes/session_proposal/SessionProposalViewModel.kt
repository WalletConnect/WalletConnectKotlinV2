package com.walletconnect.sample.wallet.ui.routes.dialog_routes.session_proposal

import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.sample.wallet.domain.accounts
import com.walletconnect.sample.wallet.ui.common.peer.PeerUI
import com.walletconnect.sample_common.Chains
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SessionProposalViewModel : ViewModel() {
    val sessionProposal: SessionProposalUI? = generateSessionProposalUI(Web3Wallet.getSessionProposals().last())

    suspend fun approve() {
        return suspendCoroutine { continuation ->
            if (Web3Wallet.getSessionProposals().isNotEmpty()) {

                val sessionProposal: Wallet.Model.SessionProposal = requireNotNull(Web3Wallet.getSessionProposals().last())
                val chains = walletMetaData.namespaces.flatMap { (_, proposal) -> proposal.chains!! }
                val selectedAccounts: Map<Chains, String> = chains.mapNotNull { namespaceChainId -> accounts.firstOrNull { (chain, _) -> chain.chainId == namespaceChainId } }.toMap()

                //todo: add accounts to supported namespaces
                val sessionNamespaces = Web3Wallet.buildSessionNamespaces(sessionProposal, walletMetaData.namespaces)
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
}