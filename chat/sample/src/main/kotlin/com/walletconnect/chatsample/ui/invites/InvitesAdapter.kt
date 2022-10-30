package com.walletconnect.chatsample.ui.invites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.ListItemInviteBinding
import com.walletconnect.chatsample.ui.messages.MessagesFragment
import com.walletconnect.chatsample.ui.shared.ChatUI

class InvitesAdapter(
    private val onAccept: (ChatUI) -> Unit,
    private val onReject: (id: Long?) -> Unit
) : ListAdapter<ChatUI, InvitesAdapter.ViewHolder>(DIFF_UTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ListItemInviteBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val invite = getItem(position)

        with(holder.binding) {
            ivChatIcon.setImageResource(invite.icon)
            tvUsername.text = invite.username
            tvLastMsg.text = invite.lastMessage
            ivReject.setOnClickListener { onReject(invite.id) }
            ivAccept.setOnClickListener {
                onAccept(invite)

                root.findNavController()
                    .navigate(R.id.action_invitesFragment_to_messagesFragment, bundleOf(MessagesFragment.peerNameKey to invite.username))
            }
        }
    }

    class ViewHolder(val binding: ListItemInviteBinding) : RecyclerView.ViewHolder(binding.root)

    private companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<ChatUI>() {
            override fun areItemsTheSame(oldItem: ChatUI, newItem: ChatUI): Boolean {
                return true
            }

            override fun areContentsTheSame(oldItem: ChatUI, newItem: ChatUI): Boolean {
                return true
            }
        }
    }
}