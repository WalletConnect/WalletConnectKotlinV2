package com.walletconnect.chatsample.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.walletconnect.chatsample.ChatListAdapter
import com.walletconnect.chatsample.ChatUI
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.FragmentChatListBinding
import com.walletconnect.chatsample.viewBinding

class ChatListFragment : Fragment(R.layout.fragment_chat_list) {
    private val binding by viewBinding(FragmentChatListBinding::bind)
    private val listOfChats = mutableSetOf(ChatUI(R.drawable.ic_chat_icon_2, "zeth.eth", "Chicken, Peter, you’re just a little chicken. Cheep, cheep, cheep, cheep"))
    private val chatListAdapter by lazy {
        ChatListAdapter().apply { submitList(listOfChats.toList()) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvChatLists.adapter = chatListAdapter

        binding.vCharRequestsBackground.setOnClickListener {
            findNavController().navigate(R.id.action_chatListFragment_to_chatRequestsFragment)
        }
    }
}