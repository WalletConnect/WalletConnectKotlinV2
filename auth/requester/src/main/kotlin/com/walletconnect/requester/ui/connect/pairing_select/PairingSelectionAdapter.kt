package com.walletconnect.requester.ui.connect.pairing_select

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.walletconnect.requester.databinding.ListItemPairingBinding

class PairingSelectionAdapter(
    private val listOfPairings: List<Pair<String, String>>,
    private val pairingTopic: (Int) -> Unit,
) : RecyclerView.Adapter<PairingSelectionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ListItemPairingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (peerImageUrl: String, peerName: String) = listOfPairings[position]

        Glide.with(holder.binding.root)
            .load(Uri.parse(peerImageUrl))
            .into(holder.binding.ivPeerIcon)

        holder.binding.tvPeerName.text = peerName

        holder.binding.root.setOnClickListener {
            pairingTopic(position)
        }
    }

    override fun getItemCount(): Int = listOfPairings.size

    inner class ViewHolder(val binding: ListItemPairingBinding) : RecyclerView.ViewHolder(binding.root)
}