package org.walletconnect.example.wallet.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.walletconnect.example.R
import org.walletconnect.example.databinding.SessionItemBinding

class SessionsAdapter : RecyclerView.Adapter<SessionViewHolder>() {
    var sessions: List<Session> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder =
        SessionViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.session_item, parent, false)
        )

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(sessions[position])
    }

    override fun getItemCount(): Int = sessions.size

    fun updateList(sessions: List<Session>) {
        this.sessions = sessions
        notifyDataSetChanged()
    }
}

class SessionViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val binding = SessionItemBinding.bind(view)

    fun bind(session: Session) = with(binding) {
        icon.setImageDrawable(ContextCompat.getDrawable(view.context, session.icon))
        name.text = session.name
        uri.text = session.uri
    }
}
