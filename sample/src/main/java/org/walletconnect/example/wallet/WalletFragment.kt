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
    private val sessionAdapter = SessionsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = WalletFragmentBinding.bind(view)
        setupToolbar()
        binding.sessions.adapter = sessionAdapter

        if (viewModel.activeSessions.isEmpty()) {
            sessionAdapter.updateList(DEFAULT_SESSION_LIST)
        } else {
            sessionAdapter.updateList(viewModel.activeSessions)
        }

        viewModel.eventFlow.observe(viewLifecycleOwner, { event ->
            when (event) {
                is ShowSessionProposalDialog -> {
                    SessionProposalDialog(requireContext(), { viewModel.approve() }, { viewModel.reject() }, event.proposal).run {
                        show()
                    }
                }
                is UpdateActiveSessions -> {
                    sessionAdapter.updateList(event.sessions)
                }
            }
        })
    }

    private fun setupToolbar() {
        binding.walletToolbar.title = getString(R.string.app_name)
        binding.walletToolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.qrCodeScanner) {
                findNavController().navigate(R.id.action_walletFragment_to_scannerFragment)
                true
            } else {
                false
            }
        }
    }

    private companion object {
        val DEFAULT_SESSION_LIST = listOf(
            Session(
                name = "UniSwap",
                uri = "app.uniswap.org",
                icon = R.drawable.ic_uniswap
            ),
            Session(
                name = "PancakeSwap",
                uri = "app.pancake.org",
                icon = R.drawable.ic_pancakeswap
            ),
            Session(
                name = "SushiSwap",
                uri = "app.sushiswap.org",
                icon = R.drawable.ic_sushiswap
            )
        )
    }
}