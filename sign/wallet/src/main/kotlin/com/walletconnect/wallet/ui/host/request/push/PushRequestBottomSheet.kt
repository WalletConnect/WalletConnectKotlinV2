package com.walletconnect.wallet.ui.host.request.push

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.walletconnect.sample_common.viewBinding
import com.walletconnect.wallet.PUSH_REQUEST_ARGS_NUM_KEY
import com.walletconnect.wallet.PUSH_REQUEST_KEY
import com.walletconnect.wallet.databinding.BottomsheetPushRequestBinding
import com.walletconnect.wallet.ui.SampleWalletEvents
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PushRequestBottomSheet: BottomSheetDialogFragment() {
    private val binding by viewBinding(BottomsheetPushRequestBinding::bind)
    private val viewModel: PushRequestViewModel by viewModels()

    init {
        isCancelable = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return BottomsheetPushRequestBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.takeIf {
            it.containsKey(PUSH_REQUEST_KEY) && it.containsKey(PUSH_REQUEST_ARGS_NUM_KEY) &&
                    it.getStringArrayList(PUSH_REQUEST_KEY)?.size == it.getInt(PUSH_REQUEST_ARGS_NUM_KEY)
        }?.let {
            viewModel.loadRequestData(it.getStringArrayList(PUSH_REQUEST_KEY)!!)
        }

        binding.btnReject.setOnClickListener {
            viewModel.reject()
        }

        binding.btnApprove.setOnClickListener {
            viewModel.approve()
        }

        viewModel.uiState
            .filterIsInstance<PushRequestUI.Content>()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                Glide.with(requireContext())
                    .load(state.icon)
                    .into(binding.ivPeerIcon)

                binding.tvPeerName.text = state.peerName
                binding.tvPeerDescription.text = state.peerDesc
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.event
            .filterIsInstance<SampleWalletEvents.PushRequestResponded>()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { findNavController().popBackStack() }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}