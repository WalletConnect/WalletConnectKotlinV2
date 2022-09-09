package com.walletconnect.requester.ui.session

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.walletconnect.requester.R
import com.walletconnect.requester.databinding.FragmentSessionBinding
import com.walletconnect.sample_common.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SessionFragment : Fragment(R.layout.fragment_session) {
    private val binding by viewBinding(FragmentSessionBinding::bind)
    private val viewModel: SessionViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.uiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { session ->
                applyState(session)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun applyState(authenticatedSessionDetailsUI: SessionDetailsUI) {
        with(binding) {
            Glide.with(root)
                .load(authenticatedSessionDetailsUI.icon)
                .into(sessionDetails.ivIcon)
            sessionDetails.tvChainName.text = authenticatedSessionDetailsUI.name
            sessionDetails.tvAccountAddress.text = authenticatedSessionDetailsUI.address
        }
    }
}