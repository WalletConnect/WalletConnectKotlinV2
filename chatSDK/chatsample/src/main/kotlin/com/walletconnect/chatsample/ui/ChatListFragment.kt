package com.walletconnect.chatsample.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.walletconnect.chat.client.Chat
import com.walletconnect.chatsample.ChatListAdapter
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.FragmentChatListBinding
import com.walletconnect.chatsample.tag
import com.walletconnect.chatsample.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

class ChatListFragment : Fragment(R.layout.fragment_chat_list) {
    private val binding by viewBinding(FragmentChatListBinding::bind)
    private val viewModel: ChatSharedViewModel by activityViewModels()
    private val chatListAdapter by lazy { ChatListAdapter(viewModel::getLastMessage) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvChatLists.adapter = chatListAdapter
        binding.rvChatLists.itemAnimator = null

        binding.flChatRequests.setOnClickListener {
            findNavController().navigate(R.id.action_chatListFragment_to_chatRequestsFragment)
        }

        binding.btnInvite.setOnClickListener {
            findNavController().navigate(R.id.action_chatListFragment_to_threadInviteDialogFragment)
        }

        viewModel.emittedEvents
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { event ->
                when (event) {
                    is ChatSampleEvents.OnMessage -> {
                        Snackbar.make(binding.root,
                            "New message from: ${viewModel.userNameToTopicMap.entries.single { it.value == event.topic }.key}",
                            Snackbar.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }.launchIn(lifecycleScope)

        viewModel
            .listOfMessagesStateFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { chatListAdapter.submitList(viewModel.listOfThreads.toList()) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel
            .listOfThreadsStateFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { chatListAdapter.submitList(it.toList()) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel
            .listOfInvitesStateFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { binding.flChatRequests.findViewById<TextView>(R.id.tvChatRequestsCount).text = it.size.toString() }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.register(object : Chat.Listeners.Register {
            override fun onError(error: Chat.Model.Error) {
                runBlocking(Dispatchers.Main) {
                    Log.e(tag(this), error.throwable.stackTraceToString())
                }
            }

            override fun onSuccess(publicKey: String) {
                runBlocking(Dispatchers.Main) {
                    Log.d(tag(this), "Registered successfully")
                }
            }
        })
    }
}