package com.walletconnect.chatsample.ui.threads.thread_invite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.BottomSheetThreadInviteBinding
import com.walletconnect.chatsample.ui.ChatSharedViewModel
import com.walletconnect.chatsample.ui.ChatThread
import com.walletconnect.chatsample.viewBinding


class ThreadInviteBottomSheet : BottomSheetDialogFragment() {
    private val viewModel: ChatSharedViewModel by activityViewModels()
    private val binding by viewBinding(BottomSheetThreadInviteBinding::bind)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return BottomSheetThreadInviteBinding.inflate(inflater, container, false).root
    }

    override fun onResume() {
        super.onResume()

        with(binding) {
            btnInvite.setOnClickListener {
                viewModel.invite(etContact.text.toString(), OPENING_MESSAGE) {
                    findNavController().navigate(
                        R.id.action_threadInviteDialogFragment_to_chatThreadFragment,
                        bundleOf(ChatThread.peerNameKey to etContact.text.toString()))
                }
            }

            tvCancel.setOnClickListener {
                findNavController().navigateUp()
            }

            etContact.requestFocus()
        }
    }

    companion object {
        const val OPENING_MESSAGE = "Hey!"
    }
}