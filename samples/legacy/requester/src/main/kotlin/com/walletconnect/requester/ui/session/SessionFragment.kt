package com.walletconnect.requester.ui.session

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.walletconnect.requester.R
import com.walletconnect.requester.databinding.FragmentSessionBinding
import com.walletconnect.sample_common.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class SessionFragment : Fragment(R.layout.fragment_session) {
    private val binding by viewBinding(FragmentSessionBinding::bind)
    private val viewModel: SessionViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.uiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { session: SessionDetailsUI ->
                applyState(session)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        binding.tvSignOut.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.fetchData()
    }

    private fun applyState(sessionDetailsUI: SessionDetailsUI) {
        with(binding) {
            when (sessionDetailsUI) {
                is ConnectedUI -> {
                    toggleProgress(false)
                    ivAvatar.setImageDrawable(resources.getDrawable(sessionDetailsUI.icon, null))
                    tvBalance.text = "0.0 ETH"
                    tvAddress.text = sessionDetailsUI.address.take(6) + "..." + sessionDetailsUI.address.takeLast(4)
                }
                is FetchingUI -> {
                    toggleProgress(true)
                    tvAddress.text = sessionDetailsUI.address.take(6) + "..." + sessionDetailsUI.address.takeLast(4)
                }
            }

        }
    }

    private fun toggleProgress(isFetching: Boolean) {
        with(binding) {
            progressGroup.isInvisible = !isFetching
            fetchedGroup.isVisible = !isFetching
        }
    }
}