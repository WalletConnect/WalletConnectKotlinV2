package com.walletconnect.sample.wallet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.walletconnect.sample.R
import com.walletconnect.sample.databinding.WalletFragmentBinding
import com.walletconnect.sample.wallet.ui.*
import com.walletconnect.sample.wallet.ui.dialog.SessionDetailsDialog
import com.walletconnect.sample.wallet.ui.dialog.SessionProposalDialog
import com.walletconnect.sample.wallet.ui.dialog.SessionRequestDialog
import com.walletconnect.sample.wallet.ui.dialog.UrlDialog
import com.walletconnect.walletconnectv2.client.WalletConnect

class WalletFragment : Fragment(R.layout.wallet_fragment), SessionActionListener {
    private val viewModel: WalletViewModel by activityViewModels()

    private lateinit var binding: WalletFragmentBinding
    private val sessionAdapter = SessionsAdapter(this)

    private var proposalDialog: SessionProposalDialog? = null
    private var requestDialog: SessionRequestDialog? = null

    //TODO: Add feature completed wallet sample

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = WalletFragmentBinding.bind(view)
        setupToolbar()
        binding.sessions.adapter = sessionAdapter

        viewModel.eventFlow.observe(viewLifecycleOwner) { event ->
            when (event) {
                is InitSessionsList -> sessionAdapter.updateList(event.sessions)
                is ShowSessionProposalDialog -> {
                    proposalDialog = SessionProposalDialog(
                        requireContext(),
                        viewModel::approve,
                        viewModel::reject,
                        event.proposal
                    )
                    proposalDialog?.show()
                }
                is ShowSessionRequestDialog -> {
                    requestDialog = SessionRequestDialog(
                        requireContext(),
                        { sessionRequest -> viewModel.respondRequest(sessionRequest) },
                        { sessionRequest -> viewModel.rejectRequest(sessionRequest) },
                        event.sessionRequest,
                        event.session
                    )
                    requestDialog?.show()
                }
                is UpdateActiveSessions -> {
                    proposalDialog?.dismiss()
                    sessionAdapter.updateList(event.sessions)
                }
                is RejectSession -> proposalDialog?.dismiss()
            }
        }
    }

    private fun setupToolbar() {
        binding.walletToolbar.title = getString(R.string.app_name)
        binding.walletToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.qrCodeScanner -> {
                    findNavController().navigate(R.id.action_walletFragment_to_scannerFragment)
                    true
                }
                R.id.pasteUri -> {
                    UrlDialog(requireContext(), pair = viewModel::pair).show()
                    true
                }
                else -> false
            }
        }
    }

    override fun onDisconnect(session: WalletConnect.Model.SettledSession) {
        viewModel.disconnect(session.topic)
    }

    override fun onUpdate(session: WalletConnect.Model.SettledSession) {
        viewModel.sessionUpdate(session)
    }

    override fun onUpgrade(session: WalletConnect.Model.SettledSession) {
        viewModel.sessionUpgrade(session)
    }

    override fun onPing(session: WalletConnect.Model.SettledSession) {
        viewModel.sessionPing(session)
    }

    override fun onSessionsDetails(session: WalletConnect.Model.SettledSession) {
        SessionDetailsDialog(requireContext(), session) { settledSession -> viewModel.getPendingRequests(settledSession) }.show()
    }
}