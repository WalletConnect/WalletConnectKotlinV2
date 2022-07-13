package com.walletconnect.chatsample.ui.host

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import com.walletconnect.chat.client.Chat
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.ActivityChatSampleBinding
import com.walletconnect.chatsample.tag
import com.walletconnect.chatsample.ui.ChatSampleEvents
import com.walletconnect.chatsample.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChatSampleActivity : AppCompatActivity(R.layout.activity_chat_sample) {
    private val binding by viewBinding(ActivityChatSampleBinding::inflate)
    private val viewModel: ChatSampleViewModel by viewModels()
    private var threadTopic: String? = null
    private val navHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.fcvHost) as NavHostFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.emittedEvents
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { event ->
                when (event) {
                    is ChatSampleEvents.OnInvite -> {
                        Snackbar.make(binding.root, "Invited: ${event.invite.message}", Snackbar.LENGTH_LONG).show()
                        viewModel.accept(event.id)
                    }
                    is ChatSampleEvents.OnJoined -> {
                        Snackbar.make(binding.root, "Joined: ${event.topic}", Snackbar.LENGTH_LONG).show()
                        threadTopic = event.topic
                    }
                    is ChatSampleEvents.OnMessage -> {
                        Snackbar.make(binding.root, "Message: ${event.message.message}", Snackbar.LENGTH_LONG).show()
                        threadTopic = event.topic
                    }
                    else -> Unit
                }
            }.launchIn(lifecycleScope)

        binding.btnMessage.setOnClickListener {
            threadTopic?.let {
                viewModel.message(
                    it, setOf("All right!", "Nothing much", "Get lost!", "Suuuuup", "Noice!", "( ͡~ ͜ʖ ͡° )").random()
                )
            } ?: Snackbar.make(binding.root, "Press resolve first", Snackbar.LENGTH_LONG).show()
        }

        binding.btnResolve.setOnClickListener {
            viewModel.resolve(object : Chat.Listeners.Resolve {
                override fun onSuccess(publicKey: String) {
                    onResolve(publicKey)
                }

                override fun onError(error: Chat.Model.Error) {
                    this@ChatSampleActivity.onError(error)
                }
            })
        }

        binding.btnRegister.setOnClickListener {
            viewModel.register(object : Chat.Listeners.Register {
                override fun onSuccess(publicKey: String) {
                    onRegister(publicKey)
                }

                override fun onError(error: Chat.Model.Error) {
                    this@ChatSampleActivity.onError(error)
                }
            })
        }
    }

    private fun onResolve(publicKey: String) {
        viewModel.invite(publicKey)
    }

    private fun onRegister(publicKey: String) = Snackbar.make(binding.root, "Registered: $publicKey", Snackbar.LENGTH_LONG).show()

    private fun onError(error: Chat.Model.Error) {
        Snackbar.make(binding.root, "Error: ${error.throwable.localizedMessage}", Snackbar.LENGTH_LONG).show()
        Log.e(tag(this), error.throwable.stackTraceToString())
        override fun onSupportNavigateUp(): Boolean {
            return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }