package com.walletconnect.chatsample.ui.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.walletconnect.chatsample.databinding.ListItemMessageBinding

class MessagesAdapter : ListAdapter<MessageUI, MessagesAdapter.ViewHolder>(DIFF_UTIL) {
    class ViewHolder(val binding: ListItemMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ListItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val threadUI = getItem(position)
    }

    companion object {
        private val DIFF_UTIL = object : DiffUtil.ItemCallback<MessageUI>() {
            override fun areItemsTheSame(oldItem: MessageUI, newItem: MessageUI): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: MessageUI, newItem: MessageUI): Boolean =
                oldItem.text == newItem.text && oldItem.timestamp == newItem.timestamp
        }
    }
}