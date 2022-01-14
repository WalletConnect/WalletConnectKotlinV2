package com.walletconnect.sample.wallet.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.walletconnect.sample.R
import com.walletconnect.sample.databinding.SessionItemBinding
import com.walletconnect.sample.wallet.SessionActionListener
import com.walletconnect.walletconnectv2.client.WalletConnect

class SessionsAdapter(private val listener: SessionActionListener) : RecyclerView.Adapter<SessionsAdapter.SessionViewHolder>() {
    private var sessions: List<WalletConnect.Model.SettledSession> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder =
        SessionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.session_item, parent, false), listener)

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(sessions[position])
    }

    override fun getItemCount(): Int = sessions.size

    fun updateList(sessions: List<WalletConnect.Model.SettledSession>) {
        this.sessions = sessions
        notifyDataSetChanged()
    }


    inner class SessionViewHolder(private val view: View, private val listener: SessionActionListener) : RecyclerView.ViewHolder(view) {

        private val binding = SessionItemBinding.bind(view)

        fun bind(session: WalletConnect.Model.SettledSession) = with(binding) {

            view.setOnClickListener {
                listener.onSessionsDetails(session)
            }

            Glide.with(view.context)
                .load(Uri.parse(session.peerAppMetaData?.icons?.first()))
                .into(icon)

            name.text = session.peerAppMetaData?.name
            uri.text = session.peerAppMetaData?.url

            menu.setOnClickListener {
                with(PopupMenu(view.context, menu)) {
                    menuInflater.inflate(R.menu.session_menu, menu)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.disconnect -> listener.onDisconnect(session)
                            R.id.update -> listener.onUpdate(session)
                            R.id.upgrade -> listener.onUpgrade(session)
                            R.id.ping -> listener.onPing(session)
                        }
                        true
                    }
                    show()
                }
            }
        }
    }
}
