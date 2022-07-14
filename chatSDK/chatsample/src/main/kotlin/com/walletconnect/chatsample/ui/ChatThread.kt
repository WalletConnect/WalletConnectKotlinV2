package com.walletconnect.chatsample.ui

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.FragmentChatThreadBinding
import com.walletconnect.chatsample.tag
import com.walletconnect.chatsample.ui.messages.MessageUI
import com.walletconnect.chatsample.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.*

class ChatThread : Fragment(R.layout.fragment_chat_thread) {
    private val binding by viewBinding(FragmentChatThreadBinding::bind)
    private val viewModel: ChatSharedViewModel by activityViewModels()
    private val chatThreadAdapter by lazy { ChatThreadAdapter() }

    private lateinit var peerName: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        peerName = arguments?.get(peerNameKey)?.toString() ?: "Missing PeerName"
        binding.tvPeername.text = peerName
        binding.rvChatThread.itemAnimator = null
        with(binding.rvChatThread) {
            addItemDecoration(object : RecyclerView.ItemDecoration() {
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
                viewModel.sendMessage(binding.etMessage.text.toString(), peerName)
                binding.etMessage.setText("")
            }
        }

        binding.tvThreadTime.text = "TODAY ${SimpleDateFormat("h:mm a").format(Date())}"
        binding.ivPeerIcon.setImageDrawable(resources.getDrawable(ChatSharedViewModel.ensToIconMap[peerName]!!, null))

        viewModel.listOfMessagesStateFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { messages ->
                chatThreadAdapter.submitList( messages
                    .filter { it.peerName == peerName }
                    .map { it.toThreadUI() }
                    .onEach { Log.e(tag(this), "threadUI: $it") })
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    fun MessageUI.toThreadUI(): ThreadUI = when (peerName) {
        author -> ThreadUI.Peer(ChatSharedViewModel.ensToIconMap[peerName]!!, text)
        else -> ThreadUI.Self(text)
    }

    companion object {
        const val peerNameKey = "peer_name"
    }
}