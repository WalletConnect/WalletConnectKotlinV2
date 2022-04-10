package com.walletconnect.wallet.ui.sessions.active

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.walletconnect.sample_common.BottomVerticalSpaceItemDecoration
import com.walletconnect.wallet.R
import com.walletconnect.wallet.databinding.FragmentActiveSessionsBinding
import com.walletconnect.wallet.ui.SampleWalletEvents
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ActiveSessionsFragment : Fragment(R.layout.fragment_active_sessions) {
    private var _binding: FragmentActiveSessionsBinding? = null
    private val viewModel: ActiveSessionViewModel by viewModels()
    private val activeSessionsAdapter by lazy { ActiveSessionsAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentActiveSessionsBinding.bind(view).also { _binding = it }

        with(binding.root) {
            adapter = activeSessionsAdapter
            addItemDecoration(BottomVerticalSpaceItemDecoration(16))
        }

        viewModel.activeSessionUI
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            .onEach { event ->
                when (event) {
                    is SampleWalletEvents.ActiveSessions -> activeSessionsAdapter.submitList(event.listOfActiveSessions)
                    is SampleWalletEvents.UpdateSessions -> activeSessionsAdapter.submitList(event.listOfActiveSessions)
                    else -> Unit
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}