package com.walletconnect.dapp.ui.connect.pairing_select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.walletconnect.dapp.R
import com.walletconnect.dapp.databinding.FragmentPairingSelectionBinding
import com.walletconnect.dapp.ui.BottomVerticalSpaceItemDecoration
import com.walletconnect.dapp.ui.connect.ConnectViewModel
import com.walletconnect.walletconnectv2.client.WalletConnectClient

class PairingSelectionDialogFragment : DialogFragment() {
    private val viewModel: ConnectViewModel by navGraphViewModels(R.id.connectGraph)
    private var _binding: FragmentPairingSelectionBinding? = null
    private val binding: FragmentPairingSelectionBinding
        get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPairingSelectionBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pairings = WalletConnectClient.getListOfSettledPairings().mapNotNull { pairing ->
            pairing.metaData?.let { metadata ->
                metadata.icons.first() to metadata.name
            }
        }

        with(binding.rvSettledPairings) {
            addItemDecoration(BottomVerticalSpaceItemDecoration(16))
            adapter = PairingSelectionAdapter(pairings) { pairingTopicPosition ->
                binding.clpbLoading.show()


                viewModel.connectToWallet(pairingTopicPosition)
            }
        }

        binding.btnNewPairing.setOnClickListener {
            findNavController().navigate(R.id.action_dialog_pairing_selection_to_dialog_pairing_generation)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}