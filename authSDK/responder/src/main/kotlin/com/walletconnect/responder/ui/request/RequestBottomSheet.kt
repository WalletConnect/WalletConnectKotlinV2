package com.walletconnect.responder.ui.request

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.walletconnect.responder.databinding.BottomsheetSessionProposalBinding

class RequestBottomSheet : BottomSheetDialogFragment() {
    private val viewModel: RequestViewModel by viewModels()

    init {
        isCancelable = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return BottomsheetSessionProposalBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(BottomsheetSessionProposalBinding.bind(view)) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchRequestProposal({ sessionProposal ->
            Glide.with(view)
                .load(sessionProposal.peerIcon)
                .into(ivPeerIcon)

            tvPeerName.text = sessionProposal.peerName
            tvPeerDescription.text = sessionProposal.peerDescription
            tvProposalUri.text = sessionProposal.proposalUri
            tvMessage.text = sessionProposal.message
        }, {
            Toast.makeText(requireContext(), "Unable to find request", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        })

        btnReject.setOnClickListener {
            viewModel.reject()
            findNavController().popBackStack()
        }

        btnApprove.setOnClickListener {
            viewModel.approve()
            findNavController().popBackStack()

            try {
                val trickyRequesterDeeplink = "kotlin-requester-wc:/request"
                requireActivity().startActivity(Intent(Intent.ACTION_VIEW, trickyRequesterDeeplink.toUri()))
            } catch (exception: ActivityNotFoundException) {
                // There is no app to handle deep link
            }
        }
    }
}