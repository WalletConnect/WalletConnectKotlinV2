package com.walletconnect.chatsample.ui.invites

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.FragmentInvitesBinding
import com.walletconnect.chatsample.ui.shared.ChatSharedViewModel
import com.walletconnect.chatsample.utils.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class InvitesFragment : Fragment(R.layout.fragment_invites) {
    private val binding by viewBinding(FragmentInvitesBinding::bind)
    private val viewModel: ChatSharedViewModel by activityViewModels()
    private val invitesAdapter by lazy {
        InvitesAdapter(onAccept = { chatUI -> viewModel.acceptInvitation(chatUI) }, onReject = { id -> viewModel.rejectInvitation(id) })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvChatRequests.adapter = invitesAdapter
        viewModel.listOfInvitesStateFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { chatUIList -> invitesAdapter.submitList(chatUIList) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}