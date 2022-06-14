package com.walletconnect.dapp.ui.connect.chain_select

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.snackbar.Snackbar
import com.walletconnect.dapp.R
import com.walletconnect.dapp.databinding.FragmentChainSelectionBinding
import com.walletconnect.dapp.ui.SampleDappEvents
import com.walletconnect.dapp.ui.connect.ConnectViewModel
import com.walletconnect.sample_common.BottomVerticalSpaceItemDecoration
import com.walletconnect.sample_common.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChainSelectionFragment : Fragment(R.layout.fragment_chain_selection) {
    private val binding by viewBinding(FragmentChainSelectionBinding::bind)
    private val viewModel: ConnectViewModel by navGraphViewModels(R.id.connectGraph)
    private val chainSelectionAdapter by lazy { ChainSelectionAdapter(viewModel.listOfChainUI, viewModel::updateSelectedChainUI) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.rvChains){
            adapter = chainSelectionAdapter
            addItemDecoration(BottomVerticalSpaceItemDecoration(24))
        }

        binding.btnConnect.setOnClickListener {
            if (viewModel.anyChainsSelected()) {
                if (viewModel.anySettledPairingExist()) {
                    findNavController().navigate(R.id.action_fragment_chain_selection_to_dialog_pairing_selection)
                } else {
                    findNavController().navigate(R.id.action_fragment_chain_selection_to_dialog_pairing_generation)
                }
            } else {
                Toast.makeText(requireContext(), "Please select a chain", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.navigation
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            .onEach { events ->
                when (events) {
                    is SampleDappEvents.SessionApproved -> findNavController().navigate(R.id.action_global_fragment_session)
                    is SampleDappEvents.SessionRejected -> {
                        findNavController().navigate(R.id.action_global_fragment_chain_selection)
                        Snackbar.make(binding.root, "Session was Rejected", Snackbar.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}