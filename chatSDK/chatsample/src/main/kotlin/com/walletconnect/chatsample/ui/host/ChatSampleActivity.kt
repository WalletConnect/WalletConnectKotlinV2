package com.walletconnect.chatsample.ui.host

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.ActivityChatSampleBinding
import com.walletconnect.chatsample.viewBinding

class ChatSampleActivity : AppCompatActivity(R.layout.activity_chat_sample) {
    private val binding by viewBinding(ActivityChatSampleBinding::inflate)
    private val viewModel: ChatSampleViewModel by viewModels()
    private val navHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.fcvHost) as NavHostFragment
    }

    override fun onSupportNavigateUp(): Boolean {
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}