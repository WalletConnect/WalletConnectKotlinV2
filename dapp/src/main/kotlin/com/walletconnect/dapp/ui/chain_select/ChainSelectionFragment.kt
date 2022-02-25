package com.walletconnect.dapp.ui.chain_select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.walletconnect.dapp.R
import com.walletconnect.dapp.databinding.FragmentChainSelectionBinding
import com.walletconnect.dapp.ui.DappViewModel
import com.walletconnect.dapp.ui.NavigationEvents
import com.walletconnect.walletconnectv2.client.WalletConnectClient

class ChainSelectionFragment : Fragment() {
    private val viewModel: DappViewModel by activityViewModels()
    private var _binding: FragmentChainSelectionBinding? = null
    private val binding: FragmentChainSelectionBinding
        get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChainSelectionBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvChains.adapter = ChainSelectionAdapter(viewModel.listOfChainUI) { position, isChecked ->
            viewModel.updateSelectedChainUI(position, isChecked)
        }
        binding.btnConnect.setOnClickListener {
            // TODO: Check if there are any selected chains before continuing

            if (WalletConnectClient.getListOfSettledPairings().isNotEmpty()) {
                findNavController().navigate(R.id.action_fragment_chain_selection_to_dialog_pairing_selection)
            } else {
                findNavController().navigate(R.id.action_fragment_chain_selection_to_dialog_pairing_generation)
            }
        }

        viewModel.navigation.observe(viewLifecycleOwner) { events ->
            if (events == NavigationEvents.ToSession) {
                findNavController().navigateUp()
                findNavController().navigate(R.id.action_fragment_chain_selection_to_fragment_session)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}