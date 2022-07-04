package com.walletconnect.chatsample.ui.messages

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.FragmentMessagesBinding
import com.walletconnect.chatsample.ui.threads.ThreadsViewModel
import com.walletconnect.chatsample.viewBinding

class MessagesFragment : Fragment(R.layout.fragment_messages) {

    companion object {
        fun newInstance() = MessagesFragment()
    }

    private val viewModel: ThreadsViewModel by viewModels()
    private val binding by viewBinding(FragmentMessagesBinding::bind)
    private val messagesAdapter = MessagesAdapter()

}