package org.walletconnect.example.wallet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import org.walletconnect.example.R
import org.walletconnect.example.databinding.WalletFragmentBinding
import org.walletconnect.example.wallet.ui.*

class WalletFragment : Fragment(R.layout.wallet_fragment) {
    private val viewModel: WalletViewModel by activityViewModels()
    private lateinit var binding: WalletFragmentBinding
    private val sessionAdapter = SessionsAdapter { session ->
        viewModel.disconnect(session.topic)
    }
    private var proposalDialog: SessionProposalDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = WalletFragmentBinding.bind(view)
        setupToolbar()
        binding.sessions.adapter = sessionAdapter
        sessionAdapter.updateList(viewModel.listOfSettledSessions)
        viewModel.eventFlow.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ShowSessionProposalDialog -> {
                    proposalDialog = SessionProposalDialog(
                        requireContext(),
                        viewModel::approve,
                        viewModel::reject,
                        event.proposal
                    )
                    proposalDialog?.show()
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
}