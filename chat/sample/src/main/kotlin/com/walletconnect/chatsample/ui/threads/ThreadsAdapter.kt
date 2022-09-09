package com.walletconnect.chatsample.ui.threads

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.ListItemThreadBinding
import com.walletconnect.chatsample.ui.messages.MessagesFragment
import com.walletconnect.chatsample.ui.shared.ChatUI
import com.walletconnect.chatsample.ui.shared.MessageUI

class ThreadsAdapter(val lastMessageDelegate: (String) -> MessageUI) : ListAdapter<ChatUI, ThreadsAdapter.ViewHolder>(DIFF_UTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ListItemThreadBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)

        holder.binding.ivUserIcon.setImageResource(user.icon)
        holder.binding.tvUsername.text = user.username
        holder.binding.tvLastMsg.text = lastMessageDelegate(user.username).text
        holder.binding.root.setOnClickListener {
            it.findNavController().navigate(
                R.id.action_threadsFragment_to_messagesFragment,
                bundleOf(MessagesFragment.peerNameKey to user.username)
            )
        }
    }

    class ViewHolder(val binding: ListItemThreadBinding) : RecyclerView.ViewHolder(binding.root)

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