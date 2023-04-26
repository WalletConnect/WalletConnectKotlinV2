package com.walletconnect.chatsample.ui.threads.thread_invite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.BottomSheetThreadInviteBinding
import com.walletconnect.chatsample.ui.messages.MessagesFragment
import com.walletconnect.chatsample.ui.shared.ChatSharedViewModel
import com.walletconnect.chatsample.utils.viewBinding


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
                    Toast.makeText(requireContext(), "Invitation send successfully, wait for response!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
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