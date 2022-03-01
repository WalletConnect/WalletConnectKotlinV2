package com.walletconnect.dapp.ui.connect.chain_select

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.walletconnect.dapp.databinding.ListItemChainBinding

class ChainSelectionAdapter(private val listOfChainSelectionUI: List<ChainSelectionUI>, private val onChainSelected: (Int, Boolean) -> Unit) : RecyclerView.Adapter<ChainSelectionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ListItemChainBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding.cbChain) {
            isChecked = listOfChainSelectionUI[position].isSelected
            setCompoundDrawablesRelativeWithIntrinsicBounds(listOfChainSelectionUI[position].icon, 0, 0, 0)
            compoundDrawablePadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8F, resources.displayMetrics).toInt()
            text = listOfChainSelectionUI[position].chainName

            setOnCheckedChangeListener { _, isChecked ->
                onChainSelected(position, isChecked)
            }
        }
    }

    override fun getItemCount(): Int = listOfChainSelectionUI.size

    inner class ViewHolder(val binding: ListItemChainBinding) : RecyclerView.ViewHolder(binding.root)
}