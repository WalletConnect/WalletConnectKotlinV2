package com.walletconnect.wallet.ui.host.proposal

import android.util.Log
import androidx.lifecycle.ViewModel
import com.walletconnect.sample_common.EthTestChains
import com.walletconnect.sample_common.tag
import com.walletconnect.wallet.domain.*
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient

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
            val selectedAccounts = mapOfAllAccounts[WalletDelegate.selectedChainAddressId] ?: throw Exception("Can't find account")
            requireNotNull(WalletDelegate.sessionProposal).let { sessionProposal ->
                val accounts: List<String> = selectedAccounts.filter { (chain: EthTestChains, _) ->
                    "${chain.parentChain}:${chain.chainId}" in sessionProposal.chains
                }.map { (chain: EthTestChains, accountAddress: String) ->
                    "${chain.parentChain}:${chain.chainId}:${accountAddress}"
                }
                val approveProposal = WalletConnect.Params.Approve(requireNotNull(WalletDelegate.sessionProposal), accounts)

                WalletConnectClient.approve(approveProposal) { error ->
                    Log.e(tag(this@SessionProposalViewModel), error.error.stackTraceToString())
                }
            }
        }
    }

    fun reject() {
        WalletDelegate.sessionProposal?.let { sessionProposal ->
            val rejectionReason = "Reject Session"
            val reject = WalletConnect.Params.Reject(proposal = sessionProposal, reason = rejectionReason, code = 406)

            WalletConnectClient.reject(reject) { error ->
                Log.d(tag(this@SessionProposalViewModel), "sending reject error: $error")
            }
        }
    }

    private fun generateSessionProposalEvent(sessionProposal: WalletConnect.Model.SessionProposal): SessionProposalUI {
        return SessionProposalUI(
            peerIcon = sessionProposal.icons.first().toString(),
            peerName = sessionProposal.name,
            proposalUri = sessionProposal.url,
            peerDescription = sessionProposal.description,
            chains = sessionProposal.chains.joinToString("\n"),
            methods = sessionProposal.methods.joinToString("\n")
        )
    }
}