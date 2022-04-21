package com.walletconnect.wallet.ui.host.request

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
import com.walletconnect.wallet.SESSION_REQUEST_ARGS_NUM_KEY
import com.walletconnect.wallet.SESSION_REQUEST_KEY
import com.walletconnect.wallet.databinding.BottomsheetSessionRequestBinding
import com.walletconnect.wallet.ui.SampleWalletEvents
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SessionRequestBottomSheet : BottomSheetDialogFragment() {
    private val binding by viewBinding(BottomsheetSessionRequestBinding::bind)
    private val viewModel: SessionRequestViewModel by viewModels()

    init {
        isCancelable = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return BottomsheetSessionRequestBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.takeIf {
            it.containsKey(SESSION_REQUEST_KEY) && it.containsKey(SESSION_REQUEST_ARGS_NUM_KEY) &&
                    it.getStringArrayList(SESSION_REQUEST_KEY)?.size == it.getInt(SESSION_REQUEST_ARGS_NUM_KEY)
        }?.let {
            viewModel.loadRequestData(it.getStringArrayList(SESSION_REQUEST_KEY)!!)
        }

        binding.btnReject.setOnClickListener {
            viewModel.reject()
        }

        binding.btnApprove.setOnClickListener {
            viewModel.approve()
        }

        viewModel.uiState
            .filterIsInstance<SessionRequestUI.Content>()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { uiState ->
                Glide.with(requireContext())
                    .load(uiState.icon)
                    .into(binding.ivIcon)

                binding.tvName.text = uiState.peerName
                binding.tvParams.text = uiState.param
                binding.tvChain.text = uiState.chain
                binding.tvMethods.text = uiState.method
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.event
            .filterIsInstance<SampleWalletEvents.SessionRequestResponded>()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach {
                findNavController().popBackStack()
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}