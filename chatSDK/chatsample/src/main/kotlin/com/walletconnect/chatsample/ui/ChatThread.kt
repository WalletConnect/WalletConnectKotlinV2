package com.walletconnect.chatsample.ui

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.walletconnect.chatsample.ChatThreadViewModel
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.FragmentChatThreadBinding
import com.walletconnect.chatsample.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.*

class ChatThread : Fragment(R.layout.fragment_chat_thread) {
    private val binding by viewBinding(FragmentChatThreadBinding::bind)
    private val viewModel: ChatThreadViewModel by viewModels()
    private val chatThreadAdapter by lazy { ChatThreadAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tbThread.setupWithNavController(findNavController())
        binding.tbThread.title = ""

        with(binding.rvChatThread) {
            addItemDecoration(object: RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    if (parent.getChildAdapterPosition(view) != parent.adapter?.itemCount?.minus(1)) {
                        outRect.bottom = 8
                    }
                }
            })

            adapter = chatThreadAdapter
        }

        binding.btnSend.setOnClickListener {
            if (binding.etMessage.text.isNotBlank()) {
                viewModel.addSelfMessage(binding.etMessage.text.toString())
                binding.etMessage.setText("")
            }
        }

        binding.tvThreadTime.text = "TODAY ${SimpleDateFormat("h:mm a").format(Date())}"

        viewModel.listOfMessages
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach {
                chatThreadAdapter.submitList(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }
}