package com.walletconnect.responder.ui.sessions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.walletconnect.responder.databinding.ListItemSessionBinding

class SessionsAdapter : ListAdapter<SessionUI, SessionsAdapter.ViewHolder>(DIFF_UTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ListItemSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position).icon?.let { iconUrl ->
            Glide.with(holder.binding.root)
                .load(iconUrl)
                .into(holder.binding.ivPeerIcon)
        }

        holder.binding.tvPeerName.text = getItem(position).name
        holder.binding.tvPeerUrl.text = getItem(position).url
    }

    class ViewHolder(val binding: ListItemSessionBinding) : RecyclerView.ViewHolder(binding.root)

    private companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<SessionUI>() {
            override fun areItemsTheSame(oldItem: SessionUI, newItem: SessionUI): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: SessionUI, newItem: SessionUI): Boolean {
                return oldItem.name == newItem.name && oldItem.url == newItem.url
            }
        }
    }
}