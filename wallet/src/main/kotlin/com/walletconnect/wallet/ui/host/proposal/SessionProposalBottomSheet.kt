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

    init {
        isCancelable = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return BottomsheetSessionProposalBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(BottomsheetSessionProposalBinding.bind(view)) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchSessionProposal({ sessionProposal ->
            Glide.with(view)
                .load(sessionProposal.peerIcon)
                .into(ivPeerIcon)

            tvPeerName.text = sessionProposal.peerName
            tvPeerDescription.text = sessionProposal.peerDescription
            tvProposalUri.text = sessionProposal.proposalUri
            tvChains.text = sessionProposal.chains
            tvMethods.text = sessionProposal.methods
        }, {
            Toast.makeText(requireContext(), "Unable to find proposed Session", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        })

        btnReject.setOnClickListener {
            viewModel.reject()
            findNavController().popBackStack()
        }

        btnApprove.setOnClickListener {
            viewModel.approve()
            findNavController().popBackStack()
        }
    }
}