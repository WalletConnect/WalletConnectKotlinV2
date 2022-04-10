package com.walletconnect.wallet.ui.host.proposal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.walletconnect.wallet.databinding.BottomsheetSessionProposalBinding

class SessionProposalBottomSheet : BottomSheetDialogFragment() {
    private val viewModel: SessionProposalViewModel by viewModels()
    private var _binding: BottomsheetSessionProposalBinding? = null

    init {
        isCancelable = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return BottomsheetSessionProposalBinding.inflate(inflater, container, false).also {
            _binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = BottomsheetSessionProposalBinding.bind(view)

        viewModel.fetchSessionProposal({ sessionProposal ->
            Glide.with(view)
                .load(sessionProposal.peerIcon)
                .into(binding.ivPeerIcon)

            binding.tvPeerName.text = sessionProposal.peerName
            binding.tvPeerDescription.text = sessionProposal.peerDescription
            binding.tvProposalUri.text = sessionProposal.proposalUri
            binding.tvChains.text = sessionProposal.chains
            binding.tvMethods.text = sessionProposal.methods
        }, {
            Toast.makeText(requireContext(), "Unable to find proposed Session", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        })

        binding.btnReject.setOnClickListener {
            viewModel.reject()
            findNavController().popBackStack()
        }

        binding.btnApprove.setOnClickListener {
            viewModel.approve()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}