package com.walletconnect.chatsample.ui.threads.thread_invite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.navGraphViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.BottomSheetThreadInviteBinding
import com.walletconnect.chatsample.ui.threads.ThreadsViewModel
import com.walletconnect.chatsample.viewBinding

class ThreadInviteBottomSheet : BottomSheetDialogFragment() {
    private val viewModel: ThreadsViewModel by navGraphViewModels(R.id.threadsGraph)
    private val binding by viewBinding(BottomSheetThreadInviteBinding::bind)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return BottomSheetThreadInviteBinding.inflate(inflater, container, false).root
    }

}