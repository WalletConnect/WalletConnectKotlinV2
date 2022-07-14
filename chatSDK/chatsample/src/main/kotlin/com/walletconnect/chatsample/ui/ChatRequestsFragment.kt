package com.walletconnect.chatsample.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.FragmentChatRequestsBinding
import com.walletconnect.chatsample.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChatRequestsFragment: Fragment(R.layout.fragment_chat_requests) {
    private val binding by viewBinding(FragmentChatRequestsBinding::bind)
    private val viewModel: ChatSharedViewModel by activityViewModels()
    private val chatRequestsAdapter by lazy { ChatRequestsAdapter(viewModel::acceptRequest) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvChatRequests.adapter = chatRequestsAdapter

        viewModel.listOfInvitesStateFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach {
                chatRequestsAdapter.submitList(it.toList())
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }
}