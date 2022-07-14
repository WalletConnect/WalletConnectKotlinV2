package com.walletconnect.chatsample.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.walletconnect.chatsample.databinding.ListItemPeerMessageBinding
import com.walletconnect.chatsample.databinding.ListItemSelfMessageBinding

class ChatThreadAdapter : ListAdapter<ThreadUI, ChatThreadAdapter.ViewHolder>(DIFF_UTIL) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ThreadUI.Self -> 0
            is ThreadUI.Peer -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            0 -> ViewHolder.Self(ListItemSelfMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> ViewHolder.Peer(ListItemPeerMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = getItem(position)

        when (holder) {
            is ViewHolder.Self -> holder.bind(message as ThreadUI.Self)
            is ViewHolder.Peer -> holder.bind(message as ThreadUI.Peer)
        }
    }

    sealed class ViewHolder(rootBinding: ViewBinding) : RecyclerView.ViewHolder(rootBinding.root) {

        class Self(val binding: ListItemSelfMessageBinding) : ChatThreadAdapter.ViewHolder(binding) {

            fun bind(self: ThreadUI.Self) {
                binding.tvSelfMessage.text = self.message
            }
        }

        class Peer(val binding: ListItemPeerMessageBinding) : ChatThreadAdapter.ViewHolder(binding) {

            fun bind(peer: ThreadUI.Peer) {
                binding.ivPeerIcon.setImageResource(peer.icon)
                binding.tvPeerMessage.text = peer.message
            }
        }
    }

    private companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<ThreadUI>() {
            override fun areItemsTheSame(oldItem: ThreadUI, newItem: ThreadUI): Boolean {
                return false
            }

            override fun areContentsTheSame(oldItem: ThreadUI, newItem: ThreadUI): Boolean {
                return false
            }
        }
    }
}