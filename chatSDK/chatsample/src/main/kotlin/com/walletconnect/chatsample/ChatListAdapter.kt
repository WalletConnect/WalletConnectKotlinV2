package com.walletconnect.chatsample

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.walletconnect.chatsample.databinding.ListitemChatBinding

class ChatListAdapter: ListAdapter<ChatUI, ChatListAdapter.ViewHolder>(DIFF_UTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ListitemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val iconSrc = if (position == 0) {
            R.drawable.ic_chat_icon_1
        } else {
            R.drawable.ic_chat_icon_2
        }
        val user = getItem(position)

        holder.binding.ivUserIcon.setImageResource(iconSrc)
        holder.binding.tvUsername.text = user.username
        holder.binding.tvLastMsg.text = user.lastMsg
    }

    class ViewHolder(val binding: ListitemChatBinding): RecyclerView.ViewHolder(binding.root)

    private companion object {
        val DIFF_UTIL = object: DiffUtil.ItemCallback<ChatUI>() {
            override fun areItemsTheSame(oldItem: ChatUI, newItem: ChatUI): Boolean {
                return true
            }

            override fun areContentsTheSame(oldItem: ChatUI, newItem: ChatUI): Boolean {
                return true
            }
        }
    }
}