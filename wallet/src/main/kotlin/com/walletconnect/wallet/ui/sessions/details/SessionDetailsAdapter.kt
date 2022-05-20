package com.walletconnect.wallet.ui.sessions.details

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.walletconnect.wallet.databinding.ListItemChainAccountBinding
import com.walletconnect.wallet.databinding.ListItemSelectedAccountBinding

class SessionDetailsAdapter(private val updateOnSelection: (SessionDetailsUI.Content.ChainAccountInfo.Account) -> Unit) :
    ListAdapter<SessionDetailsUI.Content.ChainAccountInfo, SessionDetailsAdapter.ViewHolder>(DIFF_UTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ListItemChainAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chainAccountInfo = getItem(position)

        holder.binding.tvChainName.text = chainAccountInfo.chainName

        Glide.with(holder.binding.root)
            .asBitmap()
            .load(chainAccountInfo.chainIcon)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val layoutInflater = LayoutInflater.from(holder.binding.root.context)
                    val drawable = BitmapDrawable(holder.binding.root.context.resources, resource)

                    chainAccountInfo.listOfAccounts.onEach { account ->
                        val radioButton = ListItemSelectedAccountBinding.inflate(layoutInflater).root.apply {
                            id = View.generateViewId()
                            setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                            isChecked = account.isSelected
                            text = account.addressTitle
                            layoutParams = RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                                setMargins(0, 0, 0, 16)
                            }
                        }

                        holder.binding.rgAccounts.addView(radioButton)
                    }

                    holder.binding.rgAccounts.setOnCheckedChangeListener { radioGroup, childViewId ->
                        val radioButton: RadioButton? = radioGroup.findViewById(childViewId)
                        chainAccountInfo.listOfAccounts.find { it.addressTitle == radioButton?.text.toString() }?.let { selectedAccount ->
                            updateOnSelection(selectedAccount)
                        }
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    inner class ViewHolder(val binding: ListItemChainAccountBinding) : RecyclerView.ViewHolder(binding.root)

    private companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<SessionDetailsUI.Content.ChainAccountInfo>() {
            override fun areItemsTheSame(oldItem: SessionDetailsUI.Content.ChainAccountInfo, newItem: SessionDetailsUI.Content.ChainAccountInfo): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: SessionDetailsUI.Content.ChainAccountInfo, newItem: SessionDetailsUI.Content.ChainAccountInfo): Boolean =
                oldItem.chainNamespace == newItem.chainNamespace && oldItem.chainReference == newItem.chainReference && oldItem.listOfAccounts.containsAll(newItem.listOfAccounts)
        }
    }
}