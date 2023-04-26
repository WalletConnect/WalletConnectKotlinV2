package com.walletconnect.chatsample.ui.threads

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
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.FragmentThreadsBinding
import com.walletconnect.chatsample.ui.shared.ChatSharedEvents
import com.walletconnect.chatsample.ui.shared.ChatSharedViewModel
import com.walletconnect.chatsample.utils.tag
import com.walletconnect.chatsample.utils.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

class ThreadsFragment : Fragment(R.layout.fragment_threads) {
    private val binding by viewBinding(FragmentThreadsBinding::bind)
    private val viewModel: ChatSharedViewModel by activityViewModels()
    private val threadsAdapter by lazy { ThreadsAdapter(viewModel::getLastMessage) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            rvChatLists.adapter = threadsAdapter
            rvChatLists.itemAnimator = null
            flChatRequests.setOnClickListener { findNavController().navigate(R.id.action_threadsFragment_to_invitesFragment) }
            btnInvite.setOnClickListener { findNavController().navigate(R.id.action_threadsFragment_to_threadInviteDialogFragment) }

            viewModel.emittedEvents
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .onEach { event ->
                    when (event) {
                        is ChatSharedEvents.OnMessage -> {
                            Snackbar.make(
                                binding.root, "New message from: ${viewModel.userNameToTopicMap.entries.single { it.value == event.message.topic }.key}",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        else -> Unit
                    }
                }.launchIn(lifecycleScope)

            viewModel
                .listOfMessagesStateFlow
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .onEach { threadsAdapter.submitList(viewModel.listOfThreads.toList()) }
                .launchIn(viewLifecycleOwner.lifecycleScope)

            viewModel
                .listOfThreadsStateFlow
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .onEach { threadsAdapter.submitList(it.toList()) }
                .launchIn(viewLifecycleOwner.lifecycleScope)

            viewModel
                .listOfInvitesStateFlow
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .onEach { flChatRequests.findViewById<TextView>(R.id.tvChatRequestsCount).text = it.size.toString() }
                .launchIn(viewLifecycleOwner.lifecycleScope)

            viewModel.register()
        }

    }
}