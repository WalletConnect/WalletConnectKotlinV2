package com.walletconnect.dapp.ui.connect.pairing_select

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.walletconnect.dapp.R
import com.walletconnect.dapp.databinding.DialogPairingSelectionBinding
import com.walletconnect.dapp.ui.connect.ConnectViewModel
import com.walletconnect.sample_common.BottomVerticalSpaceItemDecoration
import com.walletconnect.walletconnectv2.client.AuthClient

class PairingSelectionDialogFragment : DialogFragment(R.layout.dialog_pairing_selection) {
    private val viewModel: ConnectViewModel by navGraphViewModels(R.id.connectGraph)
    private var _binding: DialogPairingSelectionBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = DialogPairingSelectionBinding.bind(view).also { _binding = it }
        val pairings = AuthClient.getListOfSettledPairings().mapNotNull { pairing ->
            pairing.metaData?.let { metadata -> metadata.icons.first() to metadata.name }
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