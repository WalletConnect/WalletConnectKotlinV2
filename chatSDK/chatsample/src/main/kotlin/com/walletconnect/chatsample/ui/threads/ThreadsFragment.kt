package com.walletconnect.chatsample.ui.threads

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.FragmentThreadsBinding
import com.walletconnect.chatsample.viewBinding

class ThreadsFragment : Fragment(R.layout.fragment_threads) {

    companion object {
        fun newInstance() = ThreadsFragment()
    }

    private val viewModel: ThreadsViewModel by viewModels()
    private val binding by viewBinding(FragmentThreadsBinding::bind)
    private val adapter = ThreadsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        with(binding) {
            btnNavToInvites.setOnClickListener { findNavController().navigate(R.id.action_global_invitesFragment) }
            btnNavToMessages.setOnClickListener { findNavController().navigate(R.id.action_global_messagesFragment) }
            btnNavToThreadInvite.setOnClickListener { findNavController().navigate(R.id.action_threadsFragment_to_threadInviteDialogFragment) }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_threads, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_chat_invite -> {
                findNavController().navigate(R.id.action_threadsFragment_to_threadInviteDialogFragment)
                false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}