package com.walletconnect.chatsample.ui.messages

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.FragmentMessagesBinding
import com.walletconnect.chatsample.ui.shared.ChatSharedViewModel
import com.walletconnect.chatsample.ui.shared.MessageUI
import com.walletconnect.chatsample.utils.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.*

class MessagesFragment : Fragment(R.layout.fragment_messages) {
    private val binding by viewBinding(FragmentMessagesBinding::bind)
    private val viewModel: ChatSharedViewModel by activityViewModels()
    private val messagesAdapter by lazy { MessagesAdapter() }

    private lateinit var peerName: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() = with(binding) {
        peerName = arguments?.get(peerNameKey)?.toString() ?: "Missing PeerName"
        tvPeername.text = peerName
        rvChatThread.itemAnimator = null
        with(rvChatThread) {
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    if (parent.getChildAdapterPosition(view) != parent.adapter?.itemCount?.minus(1)) {
                        outRect.bottom = 8
                    }
                }
            })
            adapter = messagesAdapter
        }

        clBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
        btnSend.setOnClickListener {
            if (binding.etMessage.text.isNotBlank()) {
                viewModel.sendMessage(binding.etMessage.text.toString(), peerName)
                etMessage.setText("")
            }
        }

        tvThreadTime.text = "TODAY ${SimpleDateFormat("h:mm a").format(Date())}"
        ivPeerIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_chat_icon_3, null))
        viewModel.listOfMessagesStateFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { messages ->
                messagesAdapter.submitList(messages.filter { it.peerName == peerName }.map { it.toMessageBubbleUI() })
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }


    private fun MessageUI.toMessageBubbleUI(): MessageBubbleUI = when (peerName) {
        author -> MessageBubbleUI.Peer(R.drawable.ic_chat_icon_3, text)
        else -> MessageBubbleUI.Self(text)
    }

    companion object {
        const val peerNameKey = "peer_name"
    }
}