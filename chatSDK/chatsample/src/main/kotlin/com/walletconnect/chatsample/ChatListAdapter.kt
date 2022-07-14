package com.walletconnect.chatsample

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.walletconnect.chat.client.Chat
import com.walletconnect.chatsample.databinding.ListItemChatBinding
import com.walletconnect.chatsample.ui.ChatThread
import com.walletconnect.chatsample.ui.messages.MessageUI

class ChatListAdapter(val lastMessageDelegate: (String) -> MessageUI) : ListAdapter<ChatUI, ChatListAdapter.ViewHolder>(DIFF_UTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ListItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)

        holder.binding.ivUserIcon.setImageResource(user.icon)
        holder.binding.tvUsername.text = user.username
        holder.binding.tvLastMsg.text = lastMessageDelegate(user.username).text
        holder.binding.root.setOnClickListener {
            it.findNavController().navigate(
                R.id.action_chatListFragment_to_chatThreadFragment,
                bundleOf(ChatThread.peerNameKey to user.username)
            )
        }
    }

    class ViewHolder(val binding: ListItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    private companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<ChatUI>() {
            override fun areItemsTheSame(oldItem: ChatUI, newItem: ChatUI): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ChatUI, newItem: ChatUI): Boolean {
                return false
            }
        }
    }
}