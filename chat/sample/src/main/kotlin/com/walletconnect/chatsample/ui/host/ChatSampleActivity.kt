package com.walletconnect.chatsample.ui.host

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.ActivityChatSampleBinding
import com.walletconnect.chatsample.ui.shared.ChatSharedEvents
import com.walletconnect.chatsample.ui.shared.ChatSharedViewModel
import com.walletconnect.chatsample.utils.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChatSampleActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityChatSampleBinding::inflate)
    private val viewModel: ChatSharedViewModel by viewModels()
    private val navHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.fcvHost) as NavHostFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel.emittedEvents
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { event ->
                when (event) {
                    is ChatSharedEvents.OnInvite -> showOnInviteSnackbar(event)
                    else -> Unit
                }
            }.launchIn(lifecycleScope)

    }

    private fun showOnInviteSnackbar(event: ChatSharedEvents.OnInvite) = Snackbar.make(binding.root,
        "\uD83D\uDE31\uD83D\uDE31\uD83D\uDE31 You got an invite from: ${ChatSharedViewModel.accountIdToEnsMap[event.invite.account.value]}",
        Snackbar.LENGTH_LONG).show()

    override fun onSupportNavigateUp(): Boolean {
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}