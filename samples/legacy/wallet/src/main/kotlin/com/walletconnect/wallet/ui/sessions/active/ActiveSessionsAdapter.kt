package com.walletconnect.wallet.ui.sessions.active

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.walletconnect.wallet.R
import com.walletconnect.wallet.SELECTED_SESSION_TOPIC_KEY
import com.walletconnect.wallet.databinding.ListItemActiveSessionBinding

class ActiveSessionsAdapter : ListAdapter<ActiveSessionUI, ActiveSessionsAdapter.ViewHolder>(DIFF_UTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ListItemActiveSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position).icon?.let { iconUrl ->
            Glide.with(holder.binding.root)
                .load(iconUrl)
                .into(holder.binding.ivPeerIcon)
        }

        holder.binding.tvPeerName.text = getItem(position).name
        holder.binding.tvPeerUrl.text = getItem(position).url
        holder.binding.root.setOnClickListener {
            holder.binding.root.findNavController().navigate(R.id.action_fragment_active_sessions_to_fragment_selected_session, bundleOf(SELECTED_SESSION_TOPIC_KEY to getItem(position).topic))
        }
    }

    class ViewHolder(val binding: ListItemActiveSessionBinding) : RecyclerView.ViewHolder(binding.root)

    private companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<ActiveSessionUI>() {
            override fun areItemsTheSame(oldItem: ActiveSessionUI, newItem: ActiveSessionUI): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ActiveSessionUI, newItem: ActiveSessionUI): Boolean {
                return oldItem.name == newItem.name && oldItem.url == newItem.url
            }
        }
    }
}