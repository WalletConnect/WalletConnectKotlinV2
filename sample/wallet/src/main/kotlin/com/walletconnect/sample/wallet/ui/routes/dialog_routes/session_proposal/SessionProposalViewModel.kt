package com.walletconnect.sample.wallet.ui.routes.dialog_routes.session_proposal

import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.sample.wallet.domain.WCDelegate
import com.walletconnect.sample.wallet.ui.common.peer.PeerUI
import com.walletconnect.sample.wallet.ui.common.peer.toPeerUI
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SessionProposalViewModel : ViewModel() {
    val sessionProposal: SessionProposalUI? = generateSessionProposalUI()

    suspend fun approve(onRedirect: (String) -> Unit = {}) {
        return suspendCoroutine { continuation ->
            if (Web3Wallet.getSessionProposals().isNotEmpty()) {
                val sessionProposal: Wallet.Model.SessionProposal = requireNotNull(Web3Wallet.getSessionProposals().last())
                val sessionNamespaces = Web3Wallet.generateApprovedNamespaces(sessionProposal = sessionProposal, supportedNamespaces = walletMetaData.namespaces)
                val approveProposal = Wallet.Params.SessionApprove(proposerPublicKey = sessionProposal.proposerPublicKey, namespaces = sessionNamespaces)

                Web3Wallet.approveSession(approveProposal,
                    onError = { error ->
                        continuation.resumeWithException(error.throwable)
                        Firebase.crashlytics.recordException(error.throwable)
                        WCDelegate.sessionProposalEvent = null
                        onRedirect(sessionProposal.redirect)
                    },
                    onSuccess = {
                        continuation.resume(Unit)
                        WCDelegate.sessionProposalEvent = null
                        onRedirect(sessionProposal.redirect)
                    })
            }
        }
    }

    suspend fun reject(onRedirect: (String) -> Unit = {}) {
        return suspendCoroutine { continuation ->
            if (Web3Wallet.getSessionProposals().isNotEmpty()) {
                val sessionProposal: Wallet.Model.SessionProposal = requireNotNull(Web3Wallet.getSessionProposals().last())
                val rejectionReason = "Reject Session"
                val reject = Wallet.Params.SessionReject(
                    proposerPublicKey = sessionProposal.proposerPublicKey,
                    reason = rejectionReason
                )

                Web3Wallet.rejectSession(reject,
                    onSuccess = {
                        continuation.resume(Unit)
                        WCDelegate.sessionProposalEvent = null
                        onRedirect(sessionProposal.redirect)
                    },
                    onError = { error ->
                        continuation.resumeWithException(error.throwable)
                        Firebase.crashlytics.recordException(error.throwable)
                        WCDelegate.sessionProposalEvent = null
                        onRedirect(sessionProposal.redirect)
                    })
            }
        }
    }

    private fun generateSessionProposalUI(): SessionProposalUI? {
        return if (WCDelegate.sessionProposalEvent != null) {
            val (proposal, context) = WCDelegate.sessionProposalEvent!!
            SessionProposalUI(
                peerUI = PeerUI(
                    peerIcon = proposal.icons.firstOrNull().toString(),
                    peerName = proposal.name,
                    peerDescription = proposal.description,
                    peerUri = proposal.url,
                ),
                namespaces = proposal.requiredNamespaces,
                peerContext = context.toPeerUI(),
                redirect = proposal.redirect
            )
        } else null
    }
}