package com.walletconnect.responder.ui.accounts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.walletconnect.responder.databinding.ListItemAccountBinding

class AccountAdapter : ListAdapter<ChainAddressUI, AccountAdapter.ViewHolder>(DIFF_UTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ListItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.binding.root)
            .load(getItem(position).chainIcon)
            .into(holder.binding.ivChainIcon)

        holder.binding.tvChainName.text = getItem(position).chainName
        holder.binding.tvChainAddress.text = getItem(position).accountAddress
    }

    class ViewHolder(val binding: ListItemAccountBinding) : RecyclerView.ViewHolder(binding.root)

    private companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<ChainAddressUI>() {
            override fun areItemsTheSame(oldItem: ChainAddressUI, newItem: ChainAddressUI): Boolean = oldItem == newItem

            override fun areContentsTheSame(oldItem: ChainAddressUI, newItem: ChainAddressUI): Boolean =
                oldItem.chainName == newItem.chainName && oldItem.accountAddress == newItem.accountAddress
        }
    }
}