package com.walletconnect.responder.ui.sessions

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.walletconnect.responder.R
import com.walletconnect.responder.databinding.FragmentSessionsBinding
import com.walletconnect.sample_common.BottomVerticalSpaceItemDecoration
import com.walletconnect.sample_common.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SessionsFragment : Fragment(R.layout.fragment_sessions) {
    private val binding: FragmentSessionsBinding by viewBinding(FragmentSessionsBinding::bind)
    private val viewModel: SessionViewModel by viewModels()
    private val activeSessionsAdapter by lazy { SessionsAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.root) {
            adapter = activeSessionsAdapter
            addItemDecoration(BottomVerticalSpaceItemDecoration(16))
        }

        viewModel.activeSessionUI
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { listOfLatestSessions ->
                activeSessionsAdapter.submitList(listOfLatestSessions)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}