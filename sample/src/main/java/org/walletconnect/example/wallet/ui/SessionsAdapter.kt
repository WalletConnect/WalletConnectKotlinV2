package org.walletconnect.example.wallet.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.walletconnect.example.R
import org.walletconnect.example.databinding.SessionItemBinding
import org.walletconnect.walletconnectv2.client.SettledSession

class SessionsAdapter(
    private val onDisconnect: (session: SettledSession) -> Unit
) : RecyclerView.Adapter<SessionViewHolder>() {
    private var sessions: List<SettledSession> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder =
        SessionViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.session_item, parent, false),
            onDisconnect
        )

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(sessions[position])
    }

    override fun getItemCount(): Int = sessions.size

    fun updateList(sessions: List<SettledSession>) {
        this.sessions = sessions
        notifyDataSetChanged()
    }
}

class SessionViewHolder(
    private val view: View,
    val onDisconnect: (session: SettledSession) -> Unit
) :
    RecyclerView.ViewHolder(view) {

    private val binding = SessionItemBinding.bind(view)

    fun bind(session: SettledSession) = with(binding) {
        Glide.with(view.context)
            .load(Uri.parse(session.icon))
            .into(icon)

        name.text = session.name
        uri.text = session.uri

        menu.setOnClickListener {
            with(PopupMenu(view.context, menu)) {
                menuInflater.inflate(R.menu.session_menu, menu)
                setOnMenuItemClickListener { item ->
                    if (item.itemId == R.id.disconnect) {
                        onDisconnect(session)
                    }
                    true
                }
                show()
            }
        }
    }
}
