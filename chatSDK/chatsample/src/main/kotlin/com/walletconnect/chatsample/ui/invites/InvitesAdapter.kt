package com.walletconnect.chatsample.ui.invites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.walletconnect.chatsample.databinding.ListItemThreadBinding

class InvitesAdapter : ListAdapter<InvitesUI, InvitesAdapter.ViewHolder>(DIFF_UTIL) {
    class ViewHolder(val binding: ListItemThreadBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ListItemThreadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val threadUI = getItem(position)
    }

    companion object {
        private val DIFF_UTIL = object : DiffUtil.ItemCallback<InvitesUI>() {
            override fun areItemsTheSame(oldItem: InvitesUI, newItem: InvitesUI): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: InvitesUI, newItem: InvitesUI): Boolean =
                oldItem.lastMessage == newItem.lastMessage && oldItem.peerName == newItem.peerName
        }
    }
}