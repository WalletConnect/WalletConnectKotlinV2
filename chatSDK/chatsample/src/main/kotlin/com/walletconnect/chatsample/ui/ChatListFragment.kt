package com.walletconnect.chatsample.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.walletconnect.chatsample.ChatListAdapter
import com.walletconnect.chatsample.ChatUI
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.FragmentChatListBinding
import com.walletconnect.chatsample.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChatListFragment : Fragment(R.layout.fragment_chat_list) {
    private val binding by viewBinding(FragmentChatListBinding::bind)
    private val viewModel: ChatSharedViewModel by activityViewModels()
    private val chatListAdapter by lazy { ChatListAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvChatLists.adapter = chatListAdapter

        binding.flChatRequests.setOnClickListener {
            findNavController().navigate(R.id.action_chatListFragment_to_chatRequestsFragment)
        }

        viewModel.listOfChats
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach {
                chatListAdapter.submitList(it.toList())
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.listOfRequests
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach {
                binding.flChatRequests.findViewById<TextView>(R.id.tvChatRequestsCount).text = it.size.toString()
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }
}