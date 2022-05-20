package com.walletconnect.dapp.ui.session

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.walletconnect.dapp.databinding.ListItemSessionAccountBinding

class SessionAdapter(private val onAccountSelected: (String) -> Unit) : ListAdapter<SessionUI, SessionAdapter.ViewHolder>(DIFF_UTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ListItemSessionAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sessionUI = getItem(position)

        Glide.with(holder.binding.root)
            .load(sessionUI.icon)
            .into(holder.binding.ivAccountIcon)

        holder.binding.tvAccountName.text = sessionUI.name
        holder.binding.tvAccountAddress.text = sessionUI.address
        holder.binding.root.setOnClickListener {
            onAccountSelected("${sessionUI.chainNamespace}:${sessionUI.chainReference}:${sessionUI.address}")
        }
    }

    class ViewHolder(val binding: ListItemSessionAccountBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private val DIFF_UTIL = object : DiffUtil.ItemCallback<SessionUI>() {

            override fun areItemsTheSame(oldItem: SessionUI, newItem: SessionUI): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: SessionUI, newItem: SessionUI): Boolean =
                oldItem.icon == newItem.icon &&
                        oldItem.name == newItem.name &&
                        oldItem.address == newItem.address &&
                        oldItem.chainNamespace == newItem.chainNamespace &&
                        oldItem.chainReference == newItem.chainReference
        }
    }
}